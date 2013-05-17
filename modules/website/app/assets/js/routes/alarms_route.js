App.AlarmsRoute = Ember.Route.extend({
  model: function() {
    return App.Alarm.find({ account_id: 1 });
  }
});

App.AlarmRoute = LB.ObjectRoute.extend({
  name: 'alarm'
});

App.AlarmsNewRoute = LB.ObjectsNewRoute.extend({
  pluralName: 'alarms'
});
