LB = {};

window.LB = LB;

/**
 * Controllers
 */

/** Controller for a filterable array with lazy display */
// TODO: Filter using a trie for better performance
LB.FilteredArrayController = Em.ArrayController.extend({
  init: function() {
    this._super();
    this.set('size', this.get('initialSize'));
  }, 
  
  sortProperties: ['id'],
  sortAscending: true, 
  
  initialSize: 20, 
  chunkSize: 20, 

  activeId: null, 
  
  filteredContent: function() {
    var filterValue    = this.get('filterValue'), 
        filterProperty = this.get('filterProperty'), 
        size           = this.get('size');

    if(filterValue) {
      filterValue = filterValue.toLowerCase();
      console.log("Filtering list by '" + filterValue + "'");
      var filtered = this.get('arrangedContent').filter(function(item) {
        // This is equivalent to item.get(filterProperty).startsWith(value)
        return item.get(filterProperty).toLowerCase().lastIndexOf(filterValue, 0) === 0
      });
      return filtered.slice(0, size);
    } else {
      return this.get('arrangedContent').slice(0, size);
    }
  }.property('content', 'content.@each', 'filterValue', 'size'), 
  
  search: function() {
    var searchTargets = this.get('filteredContent');
    
    if(searchTargets.length == 1) {
      this.set('activeId', searchTargets.objectAt(0).get('id'));
      this.transitionTo(this.get('objectRoute'), searchTargets.objectAt(0));
    }
  },
  
  more: function() {
    var currentSize = this.get('size'),
        chunkSize   = this.get('chunkSize');
        
    // Size could be above the array length, but this is what we want, i.e.
    // array.slice(...) won't fail and we can keep a size which is a multiple of chunkSize
    this.set('size', currentSize + chunkSize);
  },
  
  less: function() {
    var currentSize = this.get('size'),
        chunkSize   = this.get('chunkSize');
        
    // Don't allow size to fall below chunkSize
    this.set('size', (currentSize - chunkSize >= chunkSize) ? (currentSize - chunkSize) : chunkSize);
  },
  
  hasMore: function() {
    return this.get('size') < this.get('length');
  }.property('size', 'length')
});


LB.ObjectController = Em.ObjectController.extend({
  init: function() {
    this._super();
    this.set('showController', this.controllerFor(this.get('name') + 'Show'));
    this.set('editController', this.controllerFor(this.get('name') + 'Edit'));
    this.set('deleteDialog', this.get('deleteConfirmMessage') + " " + this.get('name') + "?");
  },
  
  showController: null,
  editController: null,
  deleteDialog: null,

  deleteConfirmMessage: "Are you sure you want to delete this",

  isEditing: false,

  modelChanged: function() {
    var showController = this.get('showController');
    showController.set('content', this.get('content'));
  }.observes('content'),

  startEditing: function() {
    var editController = this.get('editController');
    editController.set('content', this.get('content'));
    editController.startEditing();
    this.set('isEditing', true);
  },

  stopEditing: function() {
    var editController = this.get('editController');
    editController.stopEditing();
    this.set('isEditing', false);
  },

  destroyRecord: function() {
    if (window.confirm(this.get('deleteDialog'))) {
      this.get('content').deleteRecord();
      this.get('store').commit();

      // return to the main listing page
      this.get('target.router').transitionTo(this.get('indexRoute'));
    }
  }
});

LB.ObjectEditController = Em.ObjectController.extend({
  startEditing: function() {
    // add the object to a local transaction
    var object = this.get('content');
    var transaction = object.store.transaction();
    transaction.add(object);
    this.transaction = transaction;
  },

  stopEditing: function() {
    // rollback the local transaction if it hasn't already been cleared
    if (this.transaction) {
      this.transaction.rollback();
      this.transaction = undefined;
    }
  },

  save: function() {
    this.transaction.commit();
    this.transaction = undefined;
    this.controllerFor(this.get('name')).stopEditing();
  },

  cancel: function() {
    this.controllerFor(this.get('name')).stopEditing();
  }
});

LB.ObjectNewController = Em.ObjectController.extend({
  startEditing: function() {
    // create a new record on a local transaction
    this.transaction = this.store.transaction();
    this.set('content', this.transaction.createRecord(this.get('type'), {}));
  },

  stopEditing: function() {
    // rollback the local transaction if it hasn't already been cleared
    if (this.transaction) {
      this.transaction.rollback();
      this.transaction = undefined;
    }
  },

  save: function() {
    // commit and then clear the local transaction
    this.transaction.commit();
    this.transaction = undefined;
  },

  transitionAfterSave: function() {
    // when creating new records, it's necessary to wait for the record to be assigned
    // an id before we can transition to its route (which depends on its id)
    if (this.get('content.id')) {
      this.transitionTo(this.get('objectRoute'), this.get('content'));
    }
  }.observes('content.id'),

  cancel: function() {
    this.stopEditing();
    this.transitionTo(this.get('indexRoute'));
  }
});

/**
 * Models
 */
 
LB.ImprovedModel = DS.Model.extend({
  numericId: function() {
    return parseInt(this.get('id'));
  }.property('id').cacheable()
});

/**
 * Views
 */

LB.FormView = Ember.View.extend({
  didInsertElement: function() {
    this.$("input:not([readonly='readonly']):not([disabled='disabled'])").first().focus();
  }
});

LB.ObjectInListView = Em.View.extend({
  tagName: 'li',
  classNameBindings: 'isActive:active',

  isActive: function() {
    return this.get('content.id') === this.get('controller.activeId');
  }.property('controller.activeId')
});

/**
 * Routes
 */
 
LB.ObjectRoute = Em.Route.extend({
  init: function() {
    this._super();
    this.set('plural', this.get('name') + 's');
    this.set('resourceController', this.controllerFor(this.get('name')));
    this.set('resourcesController', this.controllerFor(this.get('plural')));
  },
  
  plural: null,
  resourceController: null,
  resourcesController: null,
  
  setupController: function(controller, model) {
    // reset editing state
    // note: this is necessary here because `exit` won't be called when transitioning
    // from one object directly into another
    if (controller.get('isEditing')) {
      controller.stopEditing();
    }

    // highlight this essence as active
    this.get('resourcesController').set('activeId', model.get('id'));
  },

  exit: function() {
    this._super();
    var controller = this.get('resourceController');

    // reset editing state
    if (controller.get('isEditing')) {
      controller.stopEditing();
    }

    // un-highlight the active essence (perhaps temporarily)
    this.get('resourcesController').set('activeId', null);
  }
});
 
LB.ObjectsNewRoute = Em.Route.extend({
  init: function() {
    this._super();
    this.set('resourcesNewController', this.controllerFor(this.get('pluralName') + '.' + 'new'));
  },
  
  pluralName: null,

  model: function() {
    // Because we are maintaining a transaction locally in the controller for editing,
    // the new record needs to be created in the controller.
    return null;
  },

  setupController: function(controller) {
    controller.startEditing();
  },

  exit: function() {
    this._super();
    this.get('resourcesNewController').stopEditing();
  }
});

