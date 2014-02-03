function QueueForGraphs(queueLength) {
    this.array = new Array();
    for (var i = 0; i < queueLength; i++) {
        this.array[i] = [i, 0.0];
    }
    this.queueLength = queueLength;
}

function getData() {
    return this.array;
}

function addData(newValue) {

    //shift to left
    for (var i = 0; i < this.queueLength - 1; i++) {
        this.array[i] = [i,this.array[i + 1][1]];  // (x,y)
    }

    //add the value to the last postion
    this.array[this.queueLength - 1] = [this.queueLength - 1,newValue];
}

QueueForGraphs.prototype.get = getData;
QueueForGraphs.prototype.add = addData;
