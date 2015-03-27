var timetableApp = angular.module('timetableApp', []);

timetableApp.controller('monitorCtrl', function($scope, $interval) {
	$scope.monitor = {
		departures : []
	};

	window.MONITOR = $scope.monitor;
	
	$interval($scope.$apply(), 1000);
});

function init() {
	testWebSocket();
}

function testWebSocket() {
	var loc = window.location;
	var new_uri;
	if (loc.protocol === "https:") {
		new_uri = "wss:";
	} else {
		new_uri = "ws:";
	}
	new_uri += "//" + loc.host + "/websocket/monitors/";

	var wsUri = new_uri;

	websocket = new WebSocket(wsUri);
	websocket.onopen = function(evt) {
		onOpen(evt)
	};
	websocket.onclose = function(evt) {
		onClose(evt)
	};
	websocket.onmessage = function(evt) {
		onMessage(evt)
	};
	websocket.onerror = function(evt) {
		onError(evt)
	};
}

function onOpen(evt) {
}

function onClose(evt) {
}

function onMessage(evt) {
	var monitor = JSON.parse(evt.data);
	var arrayLength = monitor.departures.length;

	var departures = [];

	for (var i = 0; i < arrayLength; i++) {
		var departure = monitor.departures[i];

		var name = departure.linie + " -> " + departure.towards;
		var countdown = departure.countdown;

		departures.push({
			name : name,
			countdown : countdown
		});
	}

	window.MONITOR.departures = departures;
}

function onError(evt) {
}

window.addEventListener("load", init, false);
