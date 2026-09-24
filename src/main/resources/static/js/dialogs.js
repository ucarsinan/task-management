(() => {
  const dialog = document.querySelector('#task-dialog');
  if (!dialog || typeof dialog.showModal !== 'function') return;
  const content = dialog.querySelector('.dialog-content');
  let request;
  let opener;

  function close() {
    request?.abort();
    dialog.close();
  }
  dialog.querySelector('.dialog-close').addEventListener('click', close);
  dialog.addEventListener('cancel', () => request?.abort());
  dialog.addEventListener('close', () => {
    request?.abort();
    content.replaceChildren();
    opener?.focus();
  });

  function render(html) {
    const source = new DOMParser().parseFromString(html, 'text/html').querySelector('main');
    if (!source) throw new Error('Missing page content');
    const ids = new Map();
    source.querySelectorAll('[id]').forEach(element => {
      const oldId = element.id;
      element.id = `dialog-${oldId}`;
      ids.set(oldId, element.id);
    });
    source.querySelectorAll('[for], [aria-describedby], [aria-labelledby]').forEach(element => {
      for (const attribute of ['for', 'aria-describedby', 'aria-labelledby']) {
        if (element.hasAttribute(attribute)) {
          element.setAttribute(attribute, element.getAttribute(attribute).split(' ').map(id => ids.get(id) || id).join(' '));
        }
      }
    });
    source.querySelector('h1').id = 'dialog-title';
    content.replaceChildren(...Array.from(source.childNodes));
    dialog.removeAttribute('aria-busy');
    const focusTarget = content.querySelector('[aria-invalid="true"]')
      || content.querySelector('input:not([type="hidden"])')
      || content.querySelector('.cancel-link');
    (focusTarget || dialog.querySelector('.dialog-close')).focus();
  }

  async function load(url, options = {}) {
    request?.abort();
    const current = new AbortController();
    request = current;
    dialog.setAttribute('aria-busy', 'true');
    try {
      const response = await fetch(url, { ...options, signal: current.signal, credentials: 'same-origin' });
      const html = await response.text();
      if (current.signal.aborted) return;
      render(html);
    } catch (error) {
      if (error.name === 'AbortError') return;
      const title = document.createElement('h1');
      title.id = 'dialog-title';
      title.textContent = 'Ansicht nicht verfügbar';
      const message = document.createElement('p');
      message.textContent = 'Schließe den Dialog und versuche es noch einmal.';
      content.replaceChildren(title, message);
      dialog.removeAttribute('aria-busy');
      dialog.querySelector('.dialog-close').focus();
    }
  }

  document.addEventListener('click', event => {
    const link = event.target.closest('a[data-dialog]');
    if (!link || event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return;
    event.preventDefault();
    opener = link;
    const title = document.createElement('h1');
    title.id = 'dialog-title';
    title.textContent = 'Wird geladen …';
    content.replaceChildren(title);
    dialog.showModal();
    load(link.href);
  });
  dialog.addEventListener('click', event => {
    if (event.target.closest('.cancel-link, .back-link') || event.target.closest('a')?.textContent.trim() === 'Abbrechen') {
      event.preventDefault();
      close();
    }
  });
  dialog.addEventListener('submit', event => {
    const form = event.target;
    if (!new URL(form.action).pathname.endsWith('/name/preview')) return;
    event.preventDefault();
    load(form.action, { method: 'POST', body: new FormData(form) });
  });
})();
