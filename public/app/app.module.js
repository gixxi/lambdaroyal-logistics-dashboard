//controllers.js

'use strict';

var app = angular.module('app', ['ngWebsocket']);

app.controller('ctrl', function ($scope, $http, $rootScope, $websocket, $location) {
  $scope.tasks = [];
  $scope.taskStatus = {
    "SUCCEEDED" : "bar",
    "FAILED" : "bar-failed",
    "RUNNING" : "bar-running",
    "KILLED" : "bar-killed"
  };

  $scope.taskNames = [];

  $http.get('/config').then(function(res) {
    $scope.taskNames = res.data;

    $scope.tasks = [
      {"startDate":new Date(),"endDate":new Date(),"taskName":$scope.taskNames[0],"status":"RUNNING"}];



    $scope.tasks.sort(function(a, b) {
      return a.endDate - b.endDate;
    });
    $scope.maxDate = $scope.tasks[$scope.tasks.length - 1].endDate;
    $scope.tasks.sort(function(a, b) {
      return a.startDate - b.startDate;
    });
    $scope.minDate = $scope.tasks[0].startDate;

    $scope.format = "%H:%M";
    $scope.timeDomainString = "1mi";

    $scope.gantt = d3.gantt().taskTypes($scope.taskNames).taskStatus($scope.taskStatus).tickFormat($scope.format).height(450).width(document.body.offsetWidth - 250);


    $scope.gantt.timeDomainMode("fixed");

    $scope.gantt($scope.tasks);
  });


  $scope.changeTimeDomain = function(timeDomainString) {
    $scope.timeDomainString = timeDomainString;
    switch (timeDomainString) {
    case "1mi":
      $scope.format = "%H:%M:%S";
      $scope.gantt.timeDomain([ d3.time.minute.offset($scope.getEndDate(), -1), $scope.getEndDate() ]);
      break;
    case "5mi":
      $scope.format = "%H:%M:%S";
      $scope.gantt.timeDomain([ d3.time.minute.offset($scope.getEndDate(), -5), $scope.getEndDate() ]);
      break;
    case "1hr":
      $scope.format = "%H:%M:%S";
      $scope.gantt.timeDomain([ d3.time.hour.offset($scope.getEndDate(), -1), $scope.getEndDate() ]);
      break;
    case "3hr":
      $scope.format = "%H:%M";
      $scope.gantt.timeDomain([ d3.time.hour.offset($scope.getEndDate(), -3), $scope.getEndDate() ]);
      break;

    case "6hr":
      $scope.format = "%H:%M";
      $scope.gantt.timeDomain([ d3.time.hour.offset($scope.getEndDate(), -6), $scope.getEndDate() ]);
      break;

    case "1day":
      $scope.format = "%H:%M";
      $scope.gantt.timeDomain([ d3.time.day.offset($scope.getEndDate(), -1), $scope.getEndDate() ]);
      break;

    case "1week":
      $scope.format = "%a %H:%M";
      $scope.gantt.timeDomain([ d3.time.day.offset($scope.getEndDate(), -7), $scope.getEndDate() ]);
      break;
    default:
      $scope.format = "%H:%M"

    }
    $scope.gantt.tickFormat($scope.format);
    $scope.gantt.redraw($scope.tasks);
  }

  $scope.getEndDate = function() {
    var lastEndDate = Date.now();
    if ($scope.tasks.length > 0) {
      lastEndDate = $scope.tasks[$scope.tasks.length - 1].endDate;
    }

    return lastEndDate;
  }

  $scope.addTask = function() {

    var lastEndDate = $scope.getEndDate();
    var taskStatusKeys = Object.keys($scope.taskStatus);
    var taskStatusName = taskStatusKeys[Math.floor(Math.random() * taskStatusKeys.length)];
    var taskName = $scope.taskNames[Math.floor(Math.random() * $scope.taskNames.length)];

    $scope.tasks.push({
      "startDate" : d3.time.hour.offset(lastEndDate, Math.ceil(1 * Math.random())),
      "endDate" : d3.time.hour.offset(lastEndDate, (Math.ceil(Math.random() * 3)) + 1),
      "taskName" : taskName,
      "status" : taskStatusName
    });

    $scope.changeTimeDomain($scope.timeDomainString);
    $scope.gantt.redraw($scope.tasks);
  };

  $scope.removeTask = function() {
    $scope.tasks.pop();
    $scope.changeTimeDomain($scope.timeDomainString);
    $scope.gantt.redraw($scope.tasks);
  };

  var path = 'ws://' + $location.$$host + ':' + $location.$$port + '/event';
  
  $scope.ws = $websocket.$new({url: path,
                               lazy: true,
                               enqueue: true,
                               reconnect: true,
                               protocols: []});
  $scope.ws
    .$on('$close', function () {
      console.log('close ws!');
    });
  $scope.ws.$on('event', function(data) {
    var delay = data.endDate - data.startDate;
    var status = delay < 1000 ? "SUCCEEDED" : (delay < 4000) ? "RUNNING" : "FAILED";
    $scope.tasks.push({
      "startDate": new Date(data.startDate),
      "endDate": new Date(data.endDate),
      "status": status,
      "taskName": data.taskName
    });
    $scope.changeTimeDomain($scope.timeDomainString);
    $scope.gantt.redraw($scope.tasks);
    console.log('webserver sent:');
    console.log(data);
  });
  

  $scope.ws.$open();

})

  .directive('focusOn', function() {
    return function(scope, elem, attr) {
      scope.$on('focusOn', function(e, name) {
        if(name === attr.focusOn) {
          elem[0].focus();
        }
      });
    };
  })

  .factory('focus', function ($rootScope, $timeout) {
    return function(name) {
      $timeout(function (){
        $rootScope.$broadcast('focusOn', name);
      });
    }
  })

  .factory('StringBuffer', function() {
    /**
     * Fast String Buffer that avoid garbaging lots of immutable objects. comparable
     * to the StringBuffer of the java universe
     * 
     * @class
     * @returns {StringBuffer}
     */
    function StringBuffer() {
      this.buffer = [];
    };

    /**
     * Adds a string to the string buffer
     * 
     * @param string
     * @function
     */
    StringBuffer.prototype.append = function(string) {
      this.buffer.push(string);
      return this;
    };

    /**
     * Gives back the buffer content as a single string
     * 
     * @function
     */
    StringBuffer.prototype.toString = function() {
      return this.buffer.join("");
    };

    return StringBuffer;

  });
