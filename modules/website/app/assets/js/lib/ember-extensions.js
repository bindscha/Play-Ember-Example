Em.TextField.reopen({
  attributeBindings: ['required']
});

Em.TextField.reopen.call(Em.TextField, Em.I18n.TranslateableAttributes)
