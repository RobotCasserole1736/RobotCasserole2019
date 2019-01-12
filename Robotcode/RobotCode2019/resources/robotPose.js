
//Note - this PORT string must be aligned with the port the webserver is served on.
var port = "5806";
var hostname = window.location.hostname + ":" + port;

//Config - adjust this year to year
var ROBOT_W_FT = 2;
var ROBOT_L_FT = 2.5;
var FIELDPOLY_FT =
    [[0, 0],
    [11, 0],
    [13.47, 3],
    [13.47, 51],
    [11, 54],
    [-11, 54],
    [-13.47, 51],
    [-13.47, 3],
    [-11, 0],
    [0, 0]
    ];

//Render Constants
var PX_PER_FOOT = 15;
var FIELD_COLOR = '#fdd';
var BOT_COLOR = '#d22';
var CANVAS_MARGIN_PX = 20;

var ROBOT_W_PX = 0;
var ROBOT_L_PX = 0;

//Websocket variables
var dataSocket = new WebSocket("ws://" + hostname + "/ds")
var numTransmissions = 0;
var botPoseXSignalName = "botposex";
var botPoseYSignalName = "botposey";
var botPoseTSignalName = "botposet";

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

    if (data.type == "sig_list") {

        var daq_request_cmd = {};

        var poseXFound = false;
        var poseYFound = false;
        var poseTFound = false;

        for (i = 0; i < data.signals.length; i++) {
            if (data.signals[i].id == botPoseXSignalName) {
                poseXFound = true;
            } else if (data.signals[i].id == botPoseYSignalName) {
                poseYFound = true;
            } else if (data.signals[i].id == botPoseTSignalName) {
                poseTFound = true;
            }
        }

        if (poseXFound == false ||
            poseYFound == false ||
            poseTFound == false) {
            alert("ERROR from Robot PoseView: Could not find required signals to drive robot. Not starting.");
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
            this.ctx.canvas.height = max_y_px - min_y_px;
            this.ctx.canvas.width = max_x_px - min_x_px;
            this.ctx_robot.canvas.height = max_y_px - min_y_px;
            this.ctx_robot.canvas.width = max_x_px - min_x_px;

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

            //Save robot dimensions
            ROBOT_W_PX = ROBOT_W_FT * PX_PER_FOOT;
            ROBOT_L_PX = ROBOT_L_FT * PX_PER_FOOT;

            //Fire up a new DAQ for the robot

            daq_request_cmd.cmd = "addDaq";
            daq_request_cmd.id = "main";
            daq_request_cmd.tx_period_ms = "50"; //Sets the frequency of packet transmit from RIO to this client
            daq_request_cmd.samp_period_ms = "0";
            daq_request_cmd.sig_id_list = [botPoseXSignalName, botPoseYSignalName, botPoseTSignalName];

            //Request data from robot
            var sendVal = JSON.stringify(daq_request_cmd);
            dataSocket.send(sendVal);

            var sendVal = JSON.stringify({ cmd: "start" });
            dataSocket.send(sendVal);

        }

    } else if (data.type == "daq_update") {
        if (data.daq_id == "main") {

            var poseXFound = false;
            var poseYFound = false;
            var poseTFound = false;

            var poseX = 0;
            var poseY = 0;
            var poseT = 0;

            for (i = 0; i < data.signals.length; i++) {
                var signal = data.signals[i];
                if (signal.samples.length > 0) {
                    if (signal.id == botPoseXSignalName) {
                        poseXFound = true;
                        poseX = signal.samples[signal.samples.length - 1].val;
                    } else if (signal.id == botPoseYSignalName) {
                        poseYFound = true;
                        poseY = signal.samples[signal.samples.length - 1].val;
                    } else if (signal.id == botPoseTSignalName) {
                        poseTFound = true;
                        poseT = signal.samples[signal.samples.length - 1].val;
                    }
                }
            }

            if (poseXFound == true &&
                poseYFound == true &&
                poseTFound == true) {
                //Handle robot pose update
                drawRobot(this.canvas_robot,
                    this.ctx_robot,
                    poseX * PX_PER_FOOT + this.bot_origin_offset_x,
                    (ctx.canvas.height - poseY * PX_PER_FOOT) + this.bot_origin_offset_y,
                    -1 * poseT);
            }
        }
    }
}

drawRobot = function (canvas, ctx, x_pos_px, y_pos_px, rotation_deg) {
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    //Configure the appearance 


    ctx.translate(x_pos_px, y_pos_px);
    ctx.rotate(rotation_deg * Math.PI / 180);
    ctx.fillStyle = BOT_COLOR;
    ctx.fillRect(-ROBOT_W_PX / 2, -ROBOT_L_PX / 2, ROBOT_W_PX, ROBOT_L_PX);
    ctx.fillStyle = '#000';
    drawArrowhead(ctx, 0, ROBOT_L_PX / 2, 0, -ROBOT_L_PX / 3, 8);
    ctx.rotate(-1 * rotation_deg * Math.PI / 180);
    ctx.translate(-x_pos_px, -y_pos_px);
}


function drawArrowhead(context, from_x, from_y, to_x, to_y, radius) {
    var x_center = to_x;
    var y_center = to_y;

    var angle;
    var x;
    var y;

    context.beginPath();

    angle = Math.atan2(to_y - from_y, to_x - from_x)
    x = radius * Math.cos(angle) + x_center;
    y = radius * Math.sin(angle) + y_center;

    context.moveTo(x, y);

    angle += (1.0 / 3.0) * (2 * Math.PI)
    x = radius * Math.cos(angle) + x_center;
    y = radius * Math.sin(angle) + y_center;

    context.lineTo(x, y);

    angle += (1.0 / 3.0) * (2 * Math.PI)
    x = radius * Math.cos(angle) + x_center;
    y = radius * Math.sin(angle) + y_center;

    context.lineTo(x, y);

    context.closePath();

    context.fill();
}