App.AlarmsController = LB.FilteredArrayController.extend({
  sortProperties: ['numericId'],
  sortAscending: true, 
  
  chunkSize: 20,
  initialSize: 20, 
  
  filterProperty: 'designation', 
  
  objectRoute: 'alarm'
});

App.AlarmController = LB.ObjectController.extend({
  name: 'alarm', 
  objectRoute: 'alarm',
  indexRoute: 'alarms.index'
});

App.AlarmShowController = Em.ObjectController;

App.AlarmEditController = LB.ObjectEditController.extend({
  name: 'alarm'
});

App.AlarmsNewController = LB.ObjectNewController.extend({
  type: App.Alarm, 
  objectRoute: 'alarm',
  indexRoute: 'alarms.index'
});
