function Twiip(data) {
    this.author = data.author
    this.message = data.message
}

function TaskListViewModel() {
        // Data
        var self = this;
        self.twiips = ko.observableArray()

        //ko.observableArray([]);

        // Load initial state from server, convert it to Task instances, then populate self.tasks
        $.getJSON( "/api/twiips", function(allData) {
            var mappedTwiips = $.map(allData, function(data) { return new Twiip(data) });
            self.twiips(mappedTwiips);
        });
}

ko.applyBindings(new TaskListViewModel());