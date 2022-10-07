import {v4 as uuidv4} from '/webjars/uuid/dist/esm-browser/index.js';

const domParser = new DOMParser();

document.querySelectorAll('.js-book-button').forEach(addBookButtonEventListener);

function addBookButtonEventListener(bookButton) {
  bookButton.addEventListener('click', e => book(e.target.dataset.event));
}

function book(id) {
  document.querySelector(`#event-${id} .js-book-button`).classList.add('is-loading');
  fetch(`/events/${id}/tickets`, {
      method: 'POST',
      headers: {
        participant: uuidv4()
      }
    })
    .then(response => {
      if (response.status === 202) {
        alert('Ticket successfully booked');
      } else if (response.status === 409) {
        alert('Unable to book a sold out event');
      } else {
        throw new Error(`Unable to book the ticket. The server returned ${response.status}`);
      }
    })
    .catch(error => {
      console.error(error);
      alert('Unable to book the ticket!')
    })
    .then(() => hydrate(id));
}

function hydrate(id) {
  eventAsElement(id)
    .then(hydrated => document.querySelector(`#event-${id}`).replaceWith(hydrated))
    .then(() => {
      const bookButton = document.querySelector(`#event-${id} .js-book-button`);
      if (bookButton) {
        addBookButtonEventListener(bookButton);
      }
    });
}

function eventAsElement(id) {
  return eventAsDocument(id).then(document => document.querySelector(`#event-${id}`));
}

function eventAsDocument(id) {
  return fetch(`/events/${id}`, {
    headers: {
      accept: 'text/html'
    }
  })
  .then(response => {
    if (response.status === 200) {
      return response.text();
    }
    throw new Error(`Unable to fetch event. The server returned ${response.status}`)
  })
  .then(text => domParser.parseFromString(text, 'text/html'))
}

export default {
  book
}
