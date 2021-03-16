////////////
// FIELDS //
////////////

const date_pattern = new RegExp("[0-9]{4}\-[0-9]{2}\-[0-9]{2}");

const str_avg          = "avg";
const str_dates        = "dates";
const str_graph_data   = "graphData";
const str_fail         = "fail";
const str_metadata     = "metadata";
const str_moment_tuple = "momentTuple";
const str_options      = "options";
const str_std          = "std";
const str_units        = "units";
const str_value        = "value";

const str_pfx_flotr_chart       = "flotr-chart-";
const str_pfx_flotr_chart_outer = "flotr-chart-outer-";
const str_sfx_minmax            = "_MinMax";

const str_graph_axis_run_date = "Run Date";

const date_split_slash = "/";
const date_split_pound = "#";
const date_split_T = "T";

const c_darkgray = "#545454";
const c_gray     = "#dddddd";
const c_white    = "#ffffff";
const c_red      = "#ffcdcd";

/////////////
// UTILITY //
/////////////

/**
 * Returns the full name of a div for a given graph name.
 * 
 * @param {type} name_ The graph name.
 * @returns The full name of the corresponding div.
 */
function divName(name_) {
    var divName = str_pfx_flotr_chart + name_;
    return divName;
}

/**
 * Returns the full name of an outer div for a given graph name.
 * 
 * @param {type} name_ The graph name.
 * @returns The full name of the corresponding outer div.
 */
function divOuterName(name_) {
    var divName = str_pfx_flotr_chart_outer + name_;
    return divName;
}

/**
 * Generates (in HTML) the textual summary that displays underneath a graph.
 * 
 * @param {type} min_ The minimum y-value.
 * @param {type} minDate_ The date on which the minimum value occurred.
 * @param {type} max_ The maximum y-value.
 * @param {type} maxDate_ The date on which the maximum value occurred.
 * @param {type} lastRun_ The date of the last run.
 * @returns The HTML that displays this information.
 */
function generateGraphTextSummary(min_, minDate_, max_, maxDate_, lastRun_) {
    var string = "<strong>Min:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
    string = string + min_.toFixed(2);
    string = string + "</strong> occurred on ";
    string = string + minDate_;
    string = string + "<br><strong>Max:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
    string = string + max_.toFixed(2);
    string = string + "</strong> occurred on ";
    string = string + maxDate_;
    string = string + '<br>Last Run: ';
    string = string + lastRun_;
    
    return string;
}

/**
 * Determines if a given text string is of the standard Watchr format
 * for representing date information.  Typically, we just want the year/month/day
 * info (of the form "YYYY-MM-DD"), so this function returns that portion of the
 * string if it exists and it adheres to a regex that checks for YYYY-MM-DD.
 * 
 * @param {type} text_ The text string to investigate.
 * @returns The YYYY-MM-DD portion of the date, if the string is a valid date.
 */
function getDate(text_) {
    var text_holder = text_;
    text_holder = text_holder.split(date_split_slash);
    if(text_holder.length > 0) {
        text_holder = text_holder[0];
        text_holder = text_holder.split(date_split_T);
        if(text_holder.length > 0) {
            text_holder = text_holder[0];
            text_holder = text_holder.split(date_split_pound);
            if(text_holder.length > 0) {
                text_holder = text_holder[0];
            } else {
                text_holder = "";
            }
        } else {
            text_holder = "";
        }
    } else {
        text_holder = "";
    }
                        
    if(text_holder.length > 0 && date_pattern.test(text_holder)) {
        return text_holder;
    } else {
        return "";
    }
}

/////////////
// PARSING /
/////////////

/**
 * Parses performance report data and creates graphical plots.
 * 
 * @param {type} data_ JSON structure sent as a String object from the Java side.
 * @returns {undefined} No return value.
 */
function parseData(data_) {
    try {
        var graphData = data_[str_graph_data];
        console.log(graphData);

        for(var name in graphData) {
            var values = [];
            var avgs   = [];
            var stds   = [];
            var dates  = [];
            var metadatas = [];

            var fail  = graphData[name][str_options][str_fail] === "true";
            var units = graphData[name][str_options][str_units];

            for(var i in graphData[name][str_dates]) {
                var moment = graphData[name][str_dates][i];
                if(moment !== null) {
                    for(var key in moment) {
                        var date_name = key + ''; // string coercion
                        date_name = getDate(date_name);
                        if(date_name.length > 0) {
                            if(moment.hasOwnProperty(key)) {
                                if(moment[key].hasOwnProperty(str_moment_tuple)) {
                                    dates.push(date_name);
                                    
                                    var value = moment[key][str_moment_tuple];
                                    for(var innerKey in value) {
                                        if(innerKey === str_value) {
                                            values.push(value[innerKey]);
                                        } else if(innerKey === str_avg) {
                                            avgs.push(value[innerKey]);
                                        } else if(innerKey === str_std) {
                                            stds.push(value[innerKey]);
                                        }
                                    }

                                    var metadataMap = moment[key][str_metadata];
                                    metadatas.push(metadataMap);
                                }
                            }
                        }
                    }                   
                }
            }

            var div = divName(name);
            
            populatePlot(div, name, dates, units, values, avgs, stds, metadatas, fail);
        }
    } catch(err) {
        console.log(err);
    }
}

/**
 * Parses custom view data and creates graphical plots.
 * 
 * @param {type} data_ JSON structure sent as a String object from the Java side.
 * @returns {undefined} No return value.
 */
function parseCustomViewData(data_) {
    try {
        console.log(data_);
        for(let name in data_["graphData"]) {
            let nameDataset = data_["graphData"][name];
            let datasetStats = data_["graphData"][name]["stats"];
            let dates = [];
            let datasetNames = [];
            
            // First, get all possible dataset names and store them in a Set.
            let dateDataArray = nameDataset["dates"];
            for(let i = 0; i < dateDataArray.length; i++ ) {
                dates.push(dateDataArray[i]["date"]);
                for(let pointName in dateDataArray[i]["points"]) {
                    if(!datasetNames.includes(pointName)) { //Sets are apparently not supported?
                        datasetNames.push(pointName);
                    }
                }
            }

            // Loop over the data structure to get data for a single plot.
            // A nuance to note is that if we hit a date (X axis point)
            // and there is no corresponding value (Y axis point) for a
            // given dataset, it is padded with a blank string.
            let datasets = {};
            for(let i = 0; i < datasetNames.length; i++) {
                let datasetName = datasetNames[i];
                let dataset = [];
                let containsData = false;
                for(let j = 0; j < dateDataArray.length; j++ ) {
                    let dataPoint = dateDataArray[j]["points"][datasetName];
                    if(dataPoint !== undefined) {
                        dataset.push(dataPoint);
                        containsData = true;
                    } else {
                        dataset.push('NaN');
                    }
                }
                if(containsData) {
                    datasets[datasetName] = dataset;
                }
            }

            let div = divName(name);
            
            populateCustomViewPlot(div, name, dates, datasets, datasetStats);
        }
    } catch(err) {
        console.log(err);
    }
}

//////////////
// GRAPHING //
//////////////

/**
 * 
 * Creates a graphical plot.
 * 
 * @param {type} div_ The div to place the plot in.
 * @param {type} name_ The name of the plot.
 * @param {type} dates_ The list of dates.
 * @param {type} units_ The unit of measurement.
 * @param {type} values_ The list of temporal values to display.
 * @param {type} avgs_ The list of temporal averages to display.
 * @param {type} stds_ The list of temporal standard deviation points to display.
 * @param {type} metadatas_ The list of metadata maps to display on the hover text.
 * @param {type} fail_ True if the graph is in a fail state and should be rendered red.
 * @returns {undefined} No return value.
 */
function populatePlot(div_, name_, dates_, units_, values_, avgs_, stds_, metadatas_, fail_) {
    try {
        /**
         * Wait till dom's finished loading.
         */
        document.observe('dom:loaded', function () {
            try {
                var chart_div = div_;
                var name      = name_;                
                var dates     = dates_;
                var units     = units_;
                var values    = values_;
                var avg       = avgs_;
                var std       = stds_;
                var fail      = fail_;
                
                // Prepare the data.
                
                var ticks = [];
                var dateAndValues = [];
                var dateAndStd = [];
                var dateAndAvg = [];

                for(let i = 0; i < dates.length; i += 1) {
                    var date = dates[i];
                    if(date !== null) {
                        ticks.push([i, date]);

                        if(values[i] !== null) {
                            dateAndValues.push([i, values[i]]);
                        }
                        if(avg[i] !== null) {
                            dateAndAvg.push([i, avg[i]]);
                        }
                        if(std[i] !== null) {
                            dateAndStd.push([i, std[i]]);
                        }
                    }
                }

                if(window.globalDataLabels === undefined) {
                    window.globalDataLabels = {};
                }
                window.globalDataLabels[name] = {};

                let momentTupleData = [];
                for(let i = 0; i < ticks.length; i++) {
                    momentTupleData.push(ticks[i][1]);
                }
                window.globalDataLabels[name][str_moment_tuple] = momentTupleData;
                window.globalDataLabels[name][str_metadata] = metadatas_;

                // Generate the graph's textual summary (min, max, and last run information)

                var max_value = Math.max.apply(Math, values);
                var max_index = values.indexOf(max_value);
                let maxDate = 0;
                if(max_index !== -1) {
                    maxDate = getDate(dates[max_index]);
                }

                var min_value = Math.min.apply(Math, values);
                var min_index = values.indexOf(min_value);
                let minDate = 0;
                if(min_index !== -1) {
                    minDate = getDate(dates[min_index]);
                }

                // Set the graph's upper and lower bounds according to the max value
                // amongst regular data values, average values, and standard deviation
                // values.
                var max_avg_value = Math.max.apply(Math, avg);
                var max_std_value = Math.max.apply(Math, std);
                var min_avg_value = Math.min.apply(Math, avg);
                var min_std_value = Math.min.apply(Math, std);
                var graph_max_value = Math.max(max_value, max_avg_value, max_std_value);
                var graph_min_value = Math.min(min_value, min_avg_value, min_std_value);
                var graph_y_range = graph_max_value - graph_min_value;
                var graph_y_buffer = graph_y_range * 0.05;
                var graph_upper_bound = graph_max_value + graph_y_buffer;
                var graph_lower_bound = graph_min_value - graph_y_buffer;
                
                let lastRun = 0;
                if(values.length > 0) {
                    lastRun = getDate(dates[dates.length - 1]);
                }
                var string = generateGraphTextSummary(min_value, minDate, max_value, maxDate, lastRun);
                
                // Prepare the div.

                let id = name + str_sfx_minmax;                
                let y = document.getElementById(id);
                if(y !== null) {
                    if (minDate !== undefined && minDate !== 0) {
                        y.innerHTML = string;
                        id = divName(name);
                        let y2 = document.getElementById(id);
                        if(y2 !== null) {
                            y2.innerHTML = '';
                        }
                    }
                }
                
                if(fail) {
                    var backgroundColorDivId = divOuterName(name);
                    var backgroundColorDiv = document.getElementById(backgroundColorDivId);
                    if(backgroundColorDiv !== null) {
                        backgroundColorDiv.style.backgroundColor = c_red;
                    }
                }
                
                // Data structure for graph options.
                var options1 = {
                    HtmlText: false,
                    selection: {str_graph_axis_run_date: 'x', fps: 30},

                    yaxis: {
                        title: units,
                        titleAngle: 90,
                        min: graph_lower_bound,
                        max: graph_upper_bound
                    },

                    xaxis: {
                        title: str_graph_axis_run_date,
                        ticks: ticks,
                        labelsAngle: 90
                    },
                    grid: {
                        color: c_darkgray,        // => primary color used for outline and labels
                        backgroundColor: c_white, // => null for transparent, else color
                        tickColor: c_gray,        // => color used for the ticks
                        labelMargin: 3            // => margin in pixels
                    },
                    mouse: {
                        track: true,
                        lineColor: 'purple',
                        relative: true,
                        position: 'ne',
                        sensibility: 100, // => The smaller this value, the more precise you've to point
                        trackDecimals: 2,
                        trackFormatter: function (obj) {
                            let index = parseInt(obj.x, 10);
                            let number = obj.y;
                            if(number > 10000 || number < 1.0e-4) {
                                number = parseFloat(number).toExponential();
                            }

                            let finalTooltipText = "<b>";
                            let tooltipDate = window.globalDataLabels[name][str_moment_tuple][index];
                            finalTooltipText = finalTooltipText + tooltipDate;
                            finalTooltipText = finalTooltipText + "</b> : " + number;

                            let tooltipMetadataMap = window.globalDataLabels[name][str_metadata][index];
                            for(let key in tooltipMetadataMap) {
                                finalTooltipText = finalTooltipText + "<br> " + key + " : " + tooltipMetadataMap[key];
                            }

                            return finalTooltipText;
                        }
                    }
                };

                /**
                 * Draws a graph.
                 * 
                 * @param {type} opts JSON containing graph options
                 * @returns {undefined} No return value.
                 */
                function drawGraph(opts) {
                    try {
                        if(chart_div !== null && $(chart_div) !== null) {
                            // Clone the options, so the 'options' variable always keeps intact.
                            var o = Object.extend(Object.clone(options1), opts || {});
                            
                            // Return a new graph.
                            Flotr.draw(
                                chart_div,
                                [dateAndValues, dateAndAvg, dateAndStd],
                                o
                            );
                        }
                    } catch(err) {
                        console.log(err);
                    }
                }

                // Now, actually draw the graph.
                if(minDate !== undefined && minDate !== 0) {
                    drawGraph();

                    // Hook into the 'flotr:select' event.
                    if(chart_div !== null && $(chart_div) !== null) {
                        $(chart_div).observe('flotr:select', function (evt) {
                            try {
                                // Get the selected area coordinates passed as event memo.
                                var area = evt.memo[0];

                                /**
                                 * What happens: empty the container element, and draw a new 
                                 * graph with bounded axis. The axis correspond to the selection
                                 * you just made.
                                 */
                                drawGraph({
                                    xaxis: {min: area.x1, max: area.x2, ticks: ticks, labelsAngle: 90, title: str_graph_axis_run_date},
                                    yaxis: {title: "Time (seconds)", titleAngle: 90, min: graph_lower_bound, max: graph_upper_bound},
                                    HtmlText: false
                                });
                            } catch(err) {
                                console.log(err);
                            }
                        });
                    }
                }
            } catch(err) {
                console.log(err);
            }
        });
    } catch(err) {
        console.log(err);
    }
}

function populateCustomViewPlot(div_, name_, dates_, datasets_, stats_) {
    try {
        /**
         * Wait till dom's finished loading.
         */
        document.observe('dom:loaded', function () {
            try {
                let chart_div = div_;
                let name      = name_;                
                let dates     = dates_;
                let datasets  = datasets_;
                let stats     = stats_;
                
                // Prepare the data.
                
                let ticks = [];
                var raw_point_data = []; // Lines without governing name labels.

                for(let i = 0; i < dates.length; i += 1) {
                    var date = dates[i];
                    if(date !== null) {
                        ticks.push([i, date]);
                    }
                }

                for(let dataset in datasets) {
                    var inner_data_map = {};
                    var inner_data_array = [];
                    for(let i = 0; i < datasets[dataset].length; i++) {
                        var point = datasets[dataset][i];
                        inner_data_array.push([i, point]);
                    }
                    inner_data_map["data"] = inner_data_array;
                    inner_data_map["label"] = dataset;
                    raw_point_data.push(inner_data_map);
                }

                if(window.globalDataLabels === undefined) {
                    window.globalDataLabels = {};
                }
                window.globalDataLabels[name] = [];

                for(let i = 0; i < ticks.length; i++) {
                    window.globalDataLabels[name].push(ticks[i][1]);
                }

                let graph_y_range = stats["max"] - stats["min"] + stats["legendBuffer"];
                let graph_y_buffer = graph_y_range * 0.05;
                                                
                // Data structure for graph options.
                var options1 = {
                    HtmlText: false,
                    selection: {str_graph_axis_run_date: 'x', fps: 30},

                    yaxis: {
                        title: "Values",
                        titleAngle: 90,
                        min: stats["min"] - graph_y_buffer,
                        max: stats["max"] + graph_y_buffer + stats["legendBuffer"]
                    },

                    xaxis: {
                        title: str_graph_axis_run_date,
                        ticks: ticks,
                        labelsAngle: 90
                    },
                    grid: {
                        color: c_darkgray,        // => primary color used for outline and labels
                        backgroundColor: c_white, // => null for transparent, else color
                        tickColor: c_gray,        // => color used for the ticks
                        labelMargin: 3            // => margin in pixels
                    },
                    mouse: {
                        track: true,
                        lineColor: 'purple',
                        relative: true,
                        position: 'ne',
                        sensibility: 100, // => The smaller this value, the more precise you've to point
                        trackDecimals: 2,
                        trackFormatter: function (obj) {
                            var theDate = window.globalDataLabels[name][parseInt(obj.x, 10)];
                            return "<b>" + theDate + "</b> : " + obj.y;
                        }
                    }
                };

                /**
                 * Draws a graph.
                 * 
                 * @param {type} opts JSON containing graph options
                 * @returns {undefined} No return value.
                 */
                function drawGraph(opts) {
                    try {
                        if(chart_div !== null && $(chart_div) !== null) {
                            // Clone the options, so the 'options' variable always keeps intact.
                            var o = Object.extend(Object.clone(options1), opts || {});
                            
                            // Return a new graph.
                            Flotr.draw(
                                chart_div,
                                raw_point_data,
                                o
                            );
                        }
                    } catch(err) {
                        console.log(err);
                    }
                }

                // Now, actually draw the graph.
                //if(minDate !== undefined && minDate !== 0) {
                drawGraph();

                // Hook into the 'flotr:select' event.
                if(chart_div !== null && $(chart_div) !== null) {
                    $(chart_div).observe('flotr:select', function (evt) {
                        try {
                            // Get the selected area coordinates passed as event memo.
                            var area = evt.memo[0];

                            /**
                             * What happens: empty the container element, and draw a new 
                             * graph with bounded axis. The axis correspond to the selection
                             * you just made.
                             */
                            drawGraph({
                                xaxis: {min: area.x1, max: area.x2, ticks: ticks, labelsAngle: 90, title: str_graph_axis_run_date},
                                yaxis: {title: "Time (seconds)", titleAngle: 90, min: -1000, max: 1000},
                                HtmlText: false,
                                legend: {position: 'ne', backgroundColor: '#D2E8FF'}
                            });
                        } catch(err) {
                            console.log(err);
                        }
                    });
                }
            } catch(err) {
                console.log(err);
            }
        });
    } catch(err) {
        console.log(err);
    }
}