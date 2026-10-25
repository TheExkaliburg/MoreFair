export function getInputTarget(event: Event): HTMLInputElement {
  if (!(event.target instanceof HTMLInputElement)) {
    throw new TypeError("Expected an input event from an HTMLInputElement");
  }
  return event.target;
}
