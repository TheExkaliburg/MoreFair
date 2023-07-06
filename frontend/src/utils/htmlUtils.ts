export function scrollToCenter(
  element: HTMLElement,
  container: HTMLElement,
  options?: ScrollToOptions
): void {
  const containerRect = container.getBoundingClientRect();
  const elementRect = element.getBoundingClientRect();

  const containerCenter = containerRect.top + containerRect.height / 2;
  const elementCenter = elementRect.top + elementRect.height / 2;

  const scrollPosition = elementCenter - containerCenter + container.scrollTop;

  if (options === undefined) options = {} as ScrollToOptions;
  options.top = scrollPosition;

  container.scrollTo(options);
}
