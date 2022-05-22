Handlebars.registerHelper('json', (context) => {
  return JSON.stringify(context).replaceAll(',', ', ');
});