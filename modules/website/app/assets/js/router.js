App.Router.map(function() {
  this.resource('alarms', function() {
    this.route('new');
    this.resource('alarm', {path: ':alarm_id'});
  });
});
