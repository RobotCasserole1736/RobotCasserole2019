
//Note - this PORT string must be aligned with the port the webserver is served on.
var port = "5806";
var hostname = window.location.hostname + ":" + port;

//Config - adjust this year to year
var ROBOT_W_FT = 2;
var ROBOT_L_FT = 2.5;
var FIELDPOLY_FT =
    [[0, 0],[13.5, 0],[13.5, 54],[-13.5, 54],[-13.5, 0],[0, 0]];
    //RED ROCKET LEFT
var FIELDELEMENTPOLY1_FT = 
    [[-13.5, 17],[-13.5, 19.75],[-12.46, 19.15],[-12.46, 17.65],[-13.5, 17]];
    //RED ROCKET RIGHT
var FIELDELEMENTPOLY2_FT = 
    [[13.5, 17],[13.5, 19.75],[12.46, 19.15],[12.46, 17.65],[13.5, 17]];
    //BLUE ROCKET LEFT
var FIELDELEMENTPOLY3_FT = 
    [[-13.5, 37],[-13.5, 34.25],[-12.46, 34.85],[-12.46, 36.35],[-13.5, 37]];
    //BLUE ROCKET RIGHT
var FIELDELEMENTPOLY4_FT = 
    [[13.5, 37],[13.5, 34.25],[12.46, 34.85],[12.46, 36.35],[13.5, 37]];
    //RED CARGO SHIP
var FIELDELEMENTPOLY5_FT = 
    [[0, 26.25],[2.3, 26.25],[2.3, 19.14],[-2.3, 19.11],[-2.3, 26.25],[0, 26.25]];
    //BLUE CARGO SHIP
var FIELDELEMENTPOLY6_FT = 
    [[0, 27.75],[2.3, 27.75],[2.3, 34.86],[-2.3, 34.86],[-2.3, 27.75],[0, 27.75]];  
    //RED HAB
var FIELDELEMENTPOLY7_FT = 
    [[0, 0],[6.5, 0],[6.5, 4],[6.3, 4],[6.3, 7.11],[-6.3, 7.11],[-6.3, 4],[-6.5, 4],[-6.5, 0],[0, 0]];
    //BLUE HAB
var FIELDELEMENTPOLY8_FT = 
    [[0, 54],[6.5, 54],[6.5, 50],[6.3, 50],[6.3, 47],[-6.3, 47],[-6.3, 50],[-6.5, 50],[-6.5, 54],[0, 54]];
    //cargo ship lines
var FIELDTAPEPOLY1_FT = 
    [[0.9, 17.64],[0.9, 36.36],[0.7, 36.36],[0.7, 17.64],[0.9, 17.64]];

var FIELDTAPEPOLY2_FT = 
    [[-0.9, 17.64],[-0.9, 36.36],[-0.7, 36.36],[-0.7, 17.64],[-0.7, 17.64]];

var FIELDTAPEPOLY3_FT = 
    [[3.85, 21.4],[3.85, 21.6],[-3.85, 21.6],[-3.85, 21.4],[3.85, 21.4]];

var FIELDTAPEPOLY4_FT = 
    [[3.85, 23.2],[3.85, 23.4],[-3.85, 23.4],[-3.85, 23.2],[3.85, 23.2]];

var FIELDTAPEPOLY5_FT = 
    [[3.85, 25],[3.85, 25.2],[-3.85, 25.2],[-3.85, 25],[3.85, 25]];

var FIELDTAPEPOLY6_FT = 
    [[3.85, 28.5],[3.85, 28.7],[-3.85, 28.7],[-3.85, 28.5],[3.85, 28.5]];

var FIELDTAPEPOLY7_FT = 
    [[3.85, 30.3],[3.85, 30.5],[-3.85, 30.5],[-3.85, 30.3],[3.85, 30.3]];    

var FIELDTAPEPOLY8_FT = 
    [[3.85, 32.1],[3.85, 32.3],[-3.85, 32.3],[-3.85, 32.1],[3.85, 32.1]];    
    //rocket ship lines
var FIELDTAPEPOLY9_FT =
    [[13.1, 17.8],[13.2, 17.8],[12.75, 16.5],[12.65, 16.5],[13.1, 17.8]]; 
    
var FIELDTAPEPOLY10_FT =
    [[-13.1, 17.8],[-13.2, 17.8],[-12.75, 16.5],[-12.65, 16.5],[-13.1, 17.8]];

var FIELDTAPEPOLY11_FT =
    [[13, 19.3],[13.1, 19.3],[12.65, 20.3],[12.55, 20.3],[13, 19.3]]; 
    
var FIELDTAPEPOLY12_FT =
    [[-13, 19.3],[-13.1, 19.3],[-12.65, 20.3],[-12.55, 20.3],[-13, 19.3]];
    
var FIELDTAPEPOLY13_FT =
    [[13.1, 36.2],[13.2, 36.2],[12.75, 37.5],[12.65, 37.5],[13.1, 36.2]];
    
var FIELDTAPEPOLY14_FT =
    [[-13.1, 36.2],[-13.2, 36.2],[-12.75, 37.5],[-12.65, 37.5],[-13.1, 36.2]];

var FIELDTAPEPOLY15_FT =
    [[13, 34.7],[13.1, 34.7],[12.65, 33.7],[12.55, 33.7],[13, 34.7]];

var FIELDTAPEPOLY16_FT =
    [[-13, 34.7],[-13.1, 34.7],[-12.65, 33.7],[-12.55, 33.7],[-13, 34.7]];

var FIELDTAPEPOLY17_FT =
    [[12.46, 18.3],[10.96, 18.3],[10.96, 18.5],[12.46, 18.5],[12.46, 18.3]];    

var FIELDTAPEPOLY18_FT =
    [[-12.46, 18.3],[-10.96, 18.3],[-10.96, 18.5],[-12.46, 18.5],[-12.46, 18.3]];

var FIELDTAPEPOLY19_FT =
    [[12.46, 35.7],[10.96, 35.7],[10.96, 35.5],[12.46, 35.5],[12.46, 35.7]];

var FIELDTAPEPOLY20_FT =
    [[-12.46, 35.7],[-10.96, 35.7],[-10.96, 35.5],[-12.46, 35.5],[-12.46, 35.7]];
    //Human player station lines
var FIELDTAPEPOLY21_FT =
    [[-10.6, 0],[-10.6, 1.5],[-10.8, 1.5],[-10.8, 0],[-10.6, 0]];

var FIELDTAPEPOLY22_FT =
    [[10.6, 0],[10.6, 1.5],[10.8, 1.5],[10.8, 0],[10.6, 0]];

var FIELDTAPEPOLY23_FT =
    [[-10.6, 54],[-10.6, 52.5],[-10.8, 52.5],[-10.8, 54],[-10.6, 54]];    
    
var FIELDTAPEPOLY24_FT =
    [[10.6, 54],[10.6, 52.5],[10.8, 52.5],[10.8, 54],[10.6, 54]];    
    //Render Constants
var PX_PER_FOOT = 15;
var FIELD_COLOR = '#534F4D';
var BOT_COLOR = '#d22';
var RED_FIELD_ELEMENT_COLOR = '#FF2D00';
var BLUE_FIELD_ELEMENT_COLOR = '#004CFF';
var TAPE_COLOR = '#FFFFFF';
var CANVAS_MARGIN_PX = 20;

var ROBOT_W_PX = 0;
var ROBOT_L_PX = 0;

//Websocket variables
var dataSocket = new WebSocket("ws://" + hostname + "/ds")
var numTransmissions = 0;
var botDesPoseXSignalName = "botDesPoseX";
var botDesPoseYSignalName = "botDesPoseY";
var botDesPoseTSignalName = "botDesPoseT";
var botActPoseXSignalName = "botActPoseX";
var botActPoseYSignalName = "botActPoseY";
var botActPoseTSignalName = "botActPoseT";

var botDesPoseXSignalID = "";
var botDesPoseYSignalID = "";
var botDesPoseTSignalID = "";
var botActPoseXSignalID = "";
var botActPoseYSignalID = "";
var botActPoseTSignalID = "";

var botPrevDesPoseX = -1; 
var botPrevDesPoseY = -1;
var botPrevActPoseX = -1;
var botPrevActPoseY = -1;

function handleFileSelect(files_in) {

    var fileobj = files_in[0];
    var temp_series = [];
    var units_to_yaxis_index = [];
    var yaxis_index = 0;


    //Destroy any existing chart.
    if (global_chart) {
        global_chart.destroy();
    }

    //deep-copy the default chart options
    var options = $.extend(true, {}, dflt_options)


    var reader = new FileReader();
    reader.readAsText(fileobj);
    reader.onload = function (evt) {

        var all_lines = evt.target.result + '';
        var lines = all_lines.split('\n');
        var timestamp = 0;
        var plotter_index = 0;

        // Iterate over the lines and add categories or series
        $.each(lines, function (lineNo, line) {
            var items = line.split(',');

            // first line containes signal names. Ignore Time column.
            if (lineNo == 0) {
                plotter_index = 0;
                $.each(items, function (itemNo, item) {
                    if (itemNo > 0) {
                        if (item.length > 0) { //skip empty elements
                            temp_series.push({
                                name: item.replace(/ /g, '').trim(),
                                data: [],
                                visible: false,
                                visibility_counter: 0,
                                yAxis: 0, //temp, will be updated once the actual unit is read.
                                states: {
                                    hover: {
                                        enabled: false
                                    },
                                },
                            });
                            plotter_index++;
                        }
                    }
                });
                numSignals = plotter_index;
            }

            // second line containes units. Ignore Time column.
            else if (lineNo == 1) {
                plotter_index = 0;
                $.each(items, function (itemNo, item) {
                    if (itemNo > 0 && plotter_index < numSignals) {
                        var unit = item.replace(/ /g, '').trim();
                        temp_series[plotter_index].name = temp_series[plotter_index].name + ' (' + unit + ')';
                        if (!(unit in units_to_yaxis_index)) {
                            units_to_yaxis_index[unit] = yaxis_index;
                            options.yAxis.push({
                                title: {
                                    text: unit,
                                    style: {
                                        color: '#DDD',
                                    },
                                },
                                showEmpty: false,
                                lineColor: '#777',
                                tickColor: '#444',
                                gridLineColor: '#444',
                                gridLineWidth: 1,
                                labels: {
                                    style: {
                                        color: '#DDD',
                                        fontWeight: 'bold'
                                    },
                                },

                            });
                            yaxis_index++;
                        }
                        temp_series[plotter_index].yAxis = units_to_yaxis_index[unit];
                        plotter_index++;
                    }
                });
            }

            // the rest of the lines contain data with their name in the first 
            // position
            else {
                plotter_index = 0;
                $.each(items, function (itemNo, item) {
                    if (itemNo == 0) {
                        timestamp = parseFloat(item);
                        if (lineNo == 2) {
                            first_timestamp = timestamp;
                        }
                        last_timestamp = timestamp;

                    } else if (plotter_index < numSignals) {
                        val_str = item.trim();
                        if (val_str.length > 0) {
                            //Some data items might not have anything in them at a given timestamp
                            
                        }
                        plotter_index++;
                    }
                });
            }
        });

dataSocket.onopen = function (event) {
    document.getElementById("id01").innerHTML = "Socket Open";

    // Send the command to get the list of all signals
    dataSocket.send(JSON.stringify({ cmd: "getSig" }));
};

dataSocket.onmessage = function (event) {
    procData(event.data);
    numTransmissions = numTransmissions + 1;
    document.getElementById("id01").innerHTML = "COM Status: Socket Open. RX Count:" + numTransmissions;
};

dataSocket.onerror = function (error) {
    document.getElementById("id01").innerHTML = "COM Status: Error with socket. Reconnect to robot, open driver station, then refresh this page.";
    alert("ERROR from Robot PoseView: Robot Disconnected!!!\n\nAfter connecting to the robot, open the driver station, then refresh this page.");
};

dataSocket.onclose = function (error) {
    document.getElementById("id01").innerHTML = "COM Status: Error with socket. Reconnect to robot, open driver station, then refresh this page.";
    alert("ERROR from Robot PoseView: Robot Disconnected!!!\n\nAfter connecting to the robot, open the driver station, then refresh this page.");
};

function procData(json_data) {

    //Parse incoming websocket packet as JSON
    var data = JSON.parse(json_data);

    //Grab a reference to the canvases
    this.canvas = document.getElementById("field_bg_canvas");
    this.ctx = this.canvas.getContext("2d");

    this.canvas_robot = document.getElementById("robot_canvas");
    this.ctx_robot = this.canvas_robot.getContext("2d");

    this.canvas_path = document.getElementById("path_canvas");
    this.ctx_path = this.canvas_path.getContext("2d");

    if (data.type == "sig_list") {

        var daq_request_cmd = {};

        var desPoseXFound = false;
        var desPoseYFound = false;
        var desPoseTFound = false;
        var actPoseXFound = false;
        var actPoseYFound = false;
        var actPoseTFound = false;

        for (i = 0; i < data.signals.length; i++) {
            if (data.signals[i].display_name == botDesPoseXSignalName) {
                desPoseXFound = true;
                botDesPoseXSignalID = data.signals[i].id;
            } else if (data.signals[i].display_name == botDesPoseYSignalName) {
                desPoseYFound = true;
                botDesPoseYSignalID = data.signals[i].id;
            } else if (data.signals[i].display_name == botDesPoseTSignalName) {
                desPoseTFound = true;
                botDesPoseTSignalID = data.signals[i].id;
            } else if (data.signals[i].display_name == botActPoseXSignalName) {
                actPoseXFound = true;
                botActPoseXSignalID = data.signals[i].id;
            } else if (data.signals[i].display_name == botActPoseYSignalName) {
                actPoseYFound = true;
                botActPoseYSignalID = data.signals[i].id;
            } else if (data.signals[i].display_name == botActPoseTSignalName) {
                actPoseTFound = true;
                botActPoseTSignalID = data.signals[i].id;
            }
        }

        if (desPoseXFound == false ||
            desPoseYFound == false ||
            desPoseTFound == false ||
            actPoseXFound == false ||
            actPoseYFound == false ||
            actPoseTFound == false ) {
            alert("ERROR from Robot PoseView: Could not find all required signals to drive robot. Not starting.");
            document.getElementById("id01").innerHTML = "COM Status: Socket Open, but signals not found.";

        } else {

            //Handle view init information

            //Get extrema of the described shape and set canvas size
            max_x_px = 0;
            min_x_px = 0;
            max_y_px = 0;
            min_y_px = 0;
            for (i = 0; i < FIELDPOLY_FT.length; i++) {
                x_px = FIELDPOLY_FT[i][0] * PX_PER_FOOT;
                y_px = FIELDPOLY_FT[i][1] * PX_PER_FOOT;

                max_x_px = Math.max(x_px, max_x_px);
                min_x_px = Math.min(x_px, min_x_px);
                max_y_px = Math.max(y_px, max_y_px);
                min_y_px = Math.min(y_px, min_y_px);
            }

            //Adjust width/height of everything based on the field dimensions requested.
            this.ctx.canvas.height = max_y_px - min_y_px;
            this.ctx.canvas.width = max_x_px - min_x_px;
            this.ctx_robot.canvas.height = this.ctx.canvas.height;
            this.ctx_robot.canvas.width = this.ctx.canvas.width;
            this.ctx_path.canvas.height = this.ctx.canvas.height;
            this.ctx_path.canvas.width = this.ctx.canvas.width;
            document.getElementById("id02").style.height = this.ctx.canvas.height;
            document.getElementById("id02").style.width = this.ctx.canvas.width;

            this.bot_origin_offset_x = -1 * min_x_px;
            this.bot_origin_offset_y = -1 * min_y_px;

            //Configure the appearance 
            this.ctx.fillStyle = FIELD_COLOR;


            //Draw polygon based on specified points 
            this.ctx.beginPath();
            for (i = 0; i < FIELDPOLY_FT.length; i++) {
                x_px = FIELDPOLY_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDPOLY_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();

            this.ctx.fillStyle = TAPE_COLOR;
            //DRAW GAFFERS TAPE 1
            this.ctx.beginPath();
            for (i = 0; i < FIELDTAPEPOLY1_FT.length; i++) {
                x_px = FIELDTAPEPOLY1_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDTAPEPOLY1_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();

            //DRAW GAFFERS TAPE 2
            this.ctx.beginPath();
            for (i = 0; i < FIELDTAPEPOLY2_FT.length; i++) {
                x_px = FIELDTAPEPOLY2_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDTAPEPOLY2_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();

            //DRAW GAFFERS TAPE 3
            this.ctx.beginPath();
            for (i = 0; i < FIELDTAPEPOLY3_FT.length; i++) {
                x_px = FIELDTAPEPOLY3_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDTAPEPOLY3_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();

            //DRAW GAFFERS TAPE 4
            this.ctx.beginPath();
            for (i = 0; i < FIELDTAPEPOLY4_FT.length; i++) {
                x_px = FIELDTAPEPOLY4_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDTAPEPOLY4_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();

            //DRAW GAFFERS TAPE 5
            this.ctx.beginPath();
            for (i = 0; i < FIELDTAPEPOLY5_FT.length; i++) {
                x_px = FIELDTAPEPOLY5_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDTAPEPOLY5_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();
                                
            //DRAW GAFFERS TAPE 6
            this.ctx.beginPath();
            for (i = 0; i < FIELDTAPEPOLY6_FT.length; i++) {
                x_px = FIELDTAPEPOLY6_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDTAPEPOLY6_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();

            //DRAW GAFFERS TAPE 7
            this.ctx.beginPath();
            for (i = 0; i < FIELDTAPEPOLY7_FT.length; i++) {
                x_px = FIELDTAPEPOLY7_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDTAPEPOLY7_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();

            //DRAW GAFFERS TAPE 8
            this.ctx.beginPath();
            for (i = 0; i < FIELDTAPEPOLY8_FT.length; i++) {
                x_px = FIELDTAPEPOLY8_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDTAPEPOLY8_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();

           
            //DRAW GAFFERS TAPE 9
            this.ctx.beginPath();
            for (i = 0; i < FIELDTAPEPOLY9_FT.length; i++) {
                x_px = FIELDTAPEPOLY9_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDTAPEPOLY9_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();

            //DRAW GAFFERS TAPE 10
            this.ctx.beginPath();
            for (i = 0; i < FIELDTAPEPOLY10_FT.length; i++) {
                x_px = FIELDTAPEPOLY10_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDTAPEPOLY10_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();

            //DRAW GAFFERS TAPE 11
            this.ctx.beginPath();
            for (i = 0; i < FIELDTAPEPOLY11_FT.length; i++) {
                x_px = FIELDTAPEPOLY11_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDTAPEPOLY11_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();

            //DRAW GAFFERS TAPE 12
            this.ctx.beginPath();
            for (i = 0; i < FIELDTAPEPOLY12_FT.length; i++) {
                x_px = FIELDTAPEPOLY12_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDTAPEPOLY12_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();

            //DRAW GAFFERS TAPE 13
            this.ctx.beginPath();
            for (i = 0; i < FIELDTAPEPOLY13_FT.length; i++) {
                x_px = FIELDTAPEPOLY13_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDTAPEPOLY13_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();

            //DRAW GAFFERS TAPE 14
            this.ctx.beginPath();
            for (i = 0; i < FIELDTAPEPOLY14_FT.length; i++) {
                x_px = FIELDTAPEPOLY14_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDTAPEPOLY14_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();
            
            //DRAW GAFFERS TAPE 15
            this.ctx.beginPath();
            for (i = 0; i < FIELDTAPEPOLY15_FT.length; i++) {
                x_px = FIELDTAPEPOLY15_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDTAPEPOLY15_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();

            //DRAW GAFFERS TAPE 16
            this.ctx.beginPath();
            for (i = 0; i < FIELDTAPEPOLY16_FT.length; i++) {
                x_px = FIELDTAPEPOLY16_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDTAPEPOLY16_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();

            //DRAW GAFFERS TAPE 17
            this.ctx.beginPath();
            for (i = 0; i < FIELDTAPEPOLY17_FT.length; i++) {
                x_px = FIELDTAPEPOLY17_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDTAPEPOLY17_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();

            //DRAW GAFFERS TAPE 18
            this.ctx.beginPath();
            for (i = 0; i < FIELDTAPEPOLY18_FT.length; i++) {
                x_px = FIELDTAPEPOLY18_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDTAPEPOLY18_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();

            //DRAW GAFFERS TAPE 19
            this.ctx.beginPath();
            for (i = 0; i < FIELDTAPEPOLY19_FT.length; i++) {
                x_px = FIELDTAPEPOLY19_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDTAPEPOLY19_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();

            //DRAW GAFFERS TAPE 20
            this.ctx.beginPath();
            for (i = 0; i < FIELDTAPEPOLY20_FT.length; i++) {
                x_px = FIELDTAPEPOLY20_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDTAPEPOLY20_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();

            //DRAW GAFFERS TAPE 21
            this.ctx.beginPath();
            for (i = 0; i < FIELDTAPEPOLY21_FT.length; i++) {
                x_px = FIELDTAPEPOLY21_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDTAPEPOLY21_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();

            //DRAW GAFFERS TAPE 22
            this.ctx.beginPath();
            for (i = 0; i < FIELDTAPEPOLY22_FT.length; i++) {
                x_px = FIELDTAPEPOLY22_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDTAPEPOLY22_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();

            //DRAW GAFFERS TAPE 23
            this.ctx.beginPath();
            for (i = 0; i < FIELDTAPEPOLY23_FT.length; i++) {
                x_px = FIELDTAPEPOLY23_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDTAPEPOLY23_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();

            //DRAW GAFFERS TAPE 24
            this.ctx.beginPath();
            for (i = 0; i < FIELDTAPEPOLY24_FT.length; i++) {
                x_px = FIELDTAPEPOLY24_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDTAPEPOLY24_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();


            this.ctx.fillStyle = RED_FIELD_ELEMENT_COLOR;
            //draw RED ROCKET LEFT
            this.ctx.beginPath();
            for (i = 0; i < FIELDELEMENTPOLY1_FT.length; i++) {
                x_px = FIELDELEMENTPOLY1_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDELEMENTPOLY1_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();

            //DRAW RED ROCKET RIGHT
            this.ctx.beginPath();
            for (i = 0; i < FIELDELEMENTPOLY2_FT.length; i++) {
                x_px = FIELDELEMENTPOLY2_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDELEMENTPOLY2_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();


              //DRAW RED CARGO
              this.ctx.beginPath();
              for (i = 0; i < FIELDELEMENTPOLY5_FT.length; i++) {
                  x_px = FIELDELEMENTPOLY5_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                  y_px = this.ctx.canvas.height - (FIELDELEMENTPOLY5_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.
  
                  if (i == 0) {
                      this.ctx.moveTo(x_px, y_px);
                  } else {
                      this.ctx.lineTo(x_px, y_px);
                  }
              }

              this.ctx.closePath();
              this.ctx.fill();

              //DRAW RED HAB
            this.ctx.beginPath();
            for (i = 0; i < FIELDELEMENTPOLY7_FT.length; i++) {
                x_px = FIELDELEMENTPOLY7_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDELEMENTPOLY7_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();

            this.ctx.fillStyle = BLUE_FIELD_ELEMENT_COLOR;

              //DRAW BLUE ROCKET LEFT
              this.ctx.beginPath();
              for (i = 0; i < FIELDELEMENTPOLY3_FT.length; i++) {
                  x_px = FIELDELEMENTPOLY3_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                  y_px = this.ctx.canvas.height - (FIELDELEMENTPOLY3_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.
  
                  if (i == 0) {
                      this.ctx.moveTo(x_px, y_px);
                  } else {
                      this.ctx.lineTo(x_px, y_px);
                  }
              }
  
              this.ctx.closePath();
              this.ctx.fill();
  

               //DRAW BLUE ROCKET RIGHT
            this.ctx.beginPath();
            for (i = 0; i < FIELDELEMENTPOLY4_FT.length; i++) {
                x_px = FIELDELEMENTPOLY4_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDELEMENTPOLY4_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();

            //DRAW BLUE CARGO
            this.ctx.beginPath();
            for (i = 0; i < FIELDELEMENTPOLY6_FT.length; i++) {
                x_px = FIELDELEMENTPOLY6_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                y_px = this.ctx.canvas.height - (FIELDELEMENTPOLY6_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.

                if (i == 0) {
                    this.ctx.moveTo(x_px, y_px);
                } else {
                    this.ctx.lineTo(x_px, y_px);
                }
            }

            this.ctx.closePath();
            this.ctx.fill();

              //DRAW BLUE HAB
              this.ctx.beginPath();
              for (i = 0; i < FIELDELEMENTPOLY8_FT.length; i++) {
                  x_px = FIELDELEMENTPOLY8_FT[i][0] * PX_PER_FOOT + this.bot_origin_offset_x;
                  y_px = this.ctx.canvas.height - (FIELDELEMENTPOLY8_FT[i][1] * PX_PER_FOOT) + this.bot_origin_offset_y; //transform from software refrence frame to html/js canvas reference frame.
  
                  if (i == 0) {
                      this.ctx.moveTo(x_px, y_px);
                  } else {
                      this.ctx.lineTo(x_px, y_px);
                  }
              }
  
              this.ctx.closePath();
              this.ctx.fill();

              
            
            

            //Save robot dimensions
            ROBOT_W_PX = ROBOT_W_FT * PX_PER_FOOT;
            ROBOT_L_PX = ROBOT_L_FT * PX_PER_FOOT;

            //Fire up a new DAQ for the robot

            daq_request_cmd.cmd = "addDaq";
            daq_request_cmd.id = "main";
            daq_request_cmd.tx_period_ms = "50"; //Sets the frequency of packet transmit from RIO to this client
            daq_request_cmd.samp_period_ms = "0";
            daq_request_cmd.sig_id_list = [botDesPoseXSignalID, botDesPoseYSignalID, botDesPoseTSignalID,
                                           botActPoseXSignalID, botActPoseYSignalID, botActPoseTSignalID   ];

            //Request data from robot
            var sendVal = JSON.stringify(daq_request_cmd);
            dataSocket.send(sendVal);

            var sendVal = JSON.stringify({ cmd: "start" });
            dataSocket.send(sendVal);

        }

    } else if (data.type == "daq_update") {
        if (data.daq_id == "main") {

            var desPoseXFound = false;
            var desPoseYFound = false;
            var desPoseTFound = false;

            var desPoseX = 0;
            var desPoseY = 0;
            var desPoseT = 0;
			
            var actPoseXFound = false;
            var actPoseYFound = false;
            var actPoseTFound = false;

            var actPoseX = 0;
            var actPoseY = 0;
            var actPoseT = 0;

            var SimString = [[0,0,0],[15,15,15],];

            for (i = 0; i < data.signals.length; i++) {
                var signal = data.signals[i];
                if (signal.samples.length > 0) {
                    if (signal.id == botDesPoseXSignalID) {
                        desPoseXFound = true;
                        desPoseX = data.push([timestamp, parseFloat(val_str)]);
                    } else if (signal.id == botDesPoseYSignalID) {
                        desPoseYFound = true;
                        desPoseY = data.push([timestamp, parseFloat(val_str)]);
                    } else if (signal.id == botDesPoseTSignalID) {
                        desPoseTFound = true;
                        desPoseT = data.push([timestamp, parseFloat(val_str)]);
                    } else if (signal.id == botActPoseXSignalID) {
                        actPoseXFound = true;
                        actPoseX = data.push([timestamp, parseFloat(val_str)]);
                    } else if (signal.id == botActPoseYSignalID) {
                        actPoseYFound = true;
                        actPoseY = data.push([timestamp, parseFloat(val_str)]);
                    } else if (signal.id == botActPoseTSignalID) {
                        actPoseTFound = true;
                        actPoseT = data.push([timestamp, parseFloat(val_str)]);
                    }
                }
            }

            this.ctx_robot.clearRect(0, 0, this.canvas_robot.width, this.canvas_robot.height);

            if (actPoseXFound == true &&
                actPoseYFound == true &&
                actPoseTFound == true) {
                //Handle robot pose update
                poseX_px  = actPoseX * PX_PER_FOOT + this.bot_origin_offset_x;
                poseY_px  = (ctx.canvas.height - actPoseY * PX_PER_FOOT) + this.bot_origin_offset_y;
                poseT_adj = -1 * actPoseT;
                if(){    
                    drawRobot(this.ctx_robot, poseX_px, poseY_px, poseT_adj, true);
                    //Draw new line segment
                    drawPathSegment(this.ctx_path, poseX_px, poseY_px,botPrevActPoseX,botPrevActPoseY,true);
                    botPrevActPoseX = poseX_px;
                    botPrevActPoseY = poseY_px;
                }
            }


            if (desPoseXFound == true &&
                desPoseYFound == true &&
                desPoseTFound == true) {
                //Handle robot pose update
                poseX_px  = desPoseX * PX_PER_FOOT + this.bot_origin_offset_x;
                poseY_px  = (ctx.canvas.height - desPoseY * PX_PER_FOOT) + this.bot_origin_offset_y;
                poseT_adj = -1 * desPoseT;
                drawRobot(this.ctx_robot, poseX_px, poseY_px, poseT_adj, false);
                //draw new line segment
                drawPathSegment(this.ctx_path, poseX_px, poseY_px,botPrevDesPoseX,botPrevDesPoseY,false);
                botPrevDesPoseX = poseX_px;
                botPrevDesPoseY = poseY_px;
            }
        }
    }
}

drawRobot = function (ctx_in, x_pos_px, y_pos_px, rotation_deg, isActual) {

    //Draw the robot itself

    //Rotate to robot reference frame
    ctx_in.translate(x_pos_px, y_pos_px);
    ctx_in.rotate(rotation_deg * Math.PI / 180);

    //Draw robot body
	if(isActual){
        //Solid filled in red robot is for Actual
        ctx_in.beginPath();
        ctx_in.strokeStyle = "black";
        ctx_in.lineWidth = "1";
        ctx_in.rect(-ROBOT_W_PX / 2, -ROBOT_L_PX / 2, ROBOT_W_PX, ROBOT_L_PX);
        ctx_in.closePath();
        ctx_in.stroke();
        ctx_in.fillStyle = "red";
        ctx_in.fillRect(-ROBOT_W_PX / 2, -ROBOT_L_PX / 2, ROBOT_W_PX, ROBOT_L_PX);
        
	} else {
        //Outlined blue robot is for Desired
        ctx_in.beginPath();
        ctx_in.strokeStyle = "blue";
        ctx_in.lineWidth = "3";
        ctx_in.rect(-ROBOT_W_PX / 2, -ROBOT_L_PX / 2, ROBOT_W_PX, ROBOT_L_PX);
        ctx_in.closePath();
        ctx_in.stroke();

        ctx_in.beginPath();
        ctx_in.strokeStyle = "black";
        ctx_in.lineWidth = "1";
        ctx_in.rect(-ROBOT_W_PX / 2, -ROBOT_L_PX / 2, ROBOT_W_PX, ROBOT_L_PX);
        ctx_in.closePath();
        ctx_in.stroke();
    }
    
    //Draw front-of-robot arrowhead
    drawArrowhead(ctx_in, 0, ROBOT_L_PX / 2, 0, -ROBOT_L_PX / 3, 8);
    
    //Undo rotation
    ctx_in.rotate(-1 * rotation_deg * Math.PI / 180);
    ctx_in.translate(-x_pos_px, -y_pos_px);

    //Draw robot centroid marker
    ctx_in.beginPath();
    ctx_in.strokeStyle = "black";
    ctx_in.lineWidth = "1";
    ctx_in.moveTo(x_pos_px-5, y_pos_px);
    ctx_in.lineTo(x_pos_px+5, y_pos_px);
    ctx_in.moveTo(x_pos_px, y_pos_px-5);
    ctx_in.lineTo(x_pos_px, y_pos_px+5);
    ctx_in.closePath();
    ctx_in.stroke(); 
}

drawPathSegment = function(ctx_in, x_start_px, y_start_px, x_end_px, y_end_px, isActual){
    
    if(x_end_px >= 0 && y_end_px >= 0){
        //Draw the line segment for the most recent path taken
        ctx_in.beginPath();
        if(isActual){
            ctx_in.strokeStyle = "red";
            ctx_in.lineWidth = "1";
        } else {
            ctx_in.strokeStyle = "blue";
            ctx_in.lineWidth = "1";
        }
        ctx_in.moveTo(x_start_px, y_start_px);
        ctx_in.lineTo(x_end_px, y_end_px);
        ctx_in.closePath();
        ctx_in.stroke(); 
    }
}

function handlePathClearBtnClick() {
    this.ctx_path.clearRect(0, 0, this.canvas_path.width, this.canvas_path.height);
}


function drawArrowhead(context_in, from_x, from_y, to_x, to_y, radius) {
    var x_center = to_x;
    var y_center = to_y;

    var angle;
    var x;
    var y;

    context_in.beginPath();
    context_in.strokeStyle = "white";
    context_in.lineWidth = "-4";
    context_in.fillStyle = '#000';

    angle = Math.atan2(to_y - from_y, to_x - from_x)
    x = radius * Math.cos(angle) + x_center;
    y = radius * Math.sin(angle) + y_center;

    context_in.moveTo(x, y);

    angle += (1.0 / 3.0) * (2 * Math.PI)
    x = radius * Math.cos(angle) + x_center;
    y = radius * Math.sin(angle) + y_center;

    context_in.lineTo(x, y);

    angle += (1.0 / 3.0) * (2 * Math.PI)
    x = radius * Math.cos(angle) + x_center;
    y = radius * Math.sin(angle) + y_center;

    context_in.lineTo(x, y);

    context_in.closePath();

    context_in.fill();
}

     