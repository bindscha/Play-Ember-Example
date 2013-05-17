App.Alarm = LB.ImprovedModel.extend({
  date:          DS.attr('date'),
  priority:      DS.attr('number'),
  status:        DS.attr('number'),
  code:          DS.attr('string'),
  description:   DS.attr('string')
});

