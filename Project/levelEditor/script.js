var level = {};

outputJSON(level);

var selected=null;

var lastAddedCategory=null;
var lastAddedCoords=null; //single point: [x,y], multi block: [[x1,y1],[x2,y2]]

var img_els=["cota","en_fly","en_grnd","pwr-up","chkpt","goal","ungr_terr","grw_terr",
"vine","brk_terr","edgeL","edgeR","edgeU","edgeD","rEndL","rTaperL","rTaperR","rEndR",
"spikeL","spikeR","spike","spikeDirtL","spikeDirtR","spikeDirt","topright","topleft",
"bottomleft","bottomright","breakabletop","breakablebottom","breakableleft","breakableright",
"breakabletopright","breakabletopleft","breakablebottomright","breakablebottomleft",
"breakableedgethreeup","breakableedgethreeright","breakableedgethreeleft","breakableedgethreedown",
"breakabletopdown","breakableleftright","spikeLrotD","spikerotD","spikeRrotD","spikeLrotR",
"spikerotR","spikeRrotR","spikeLrotL","spikerotL","spikeRrotL","spikeDirtLrotL","spikeDirtrotL",
"spikeDirtRrotL","spikeDirtLrotR","spikeDirtrotR","spikeDirtRrotR","spikeDirtLrotD","spikeDirtrotD",
"spikeDirtRrotD"];

hideImgs();

var sw=false;
var bottomLeft=null;
var tablecreated = false;
var avatarplaced = false;
var goalPlaced = false;

/** Outputs updated JSON,removes outdated */
function outputJSON(inp) {
    console.log("Last placed "+lastAddedCategory+" at "+lastAddedCoords);
    if($("#json").has("pre").length>0){
      $("pre").remove();
    }
    document.getElementById("json").appendChild(document.createElement('pre')).innerHTML = syntaxHighlight(JSON.stringify(inp, undefined, 4));
    if(lastAddedCategory!=null){
      $("#undoBtn").prop('disabled', false);
    }
    else{
      $("#undoBtn").prop('disabled', true);
    }
}

function undoLastSelection(){
  cancelSel();

  if(lastAddedCategory==="avatar" || lastAddedCategory==="exit"){ //single blocks not in a group
    level[lastAddedCategory] = {};
    var cellid="#row"+lastAddedCoords[1].toString()+"col"+lastAddedCoords[0].toString();
    $(cellid).css("background-image","none");
  }
  else if(typeof(lastAddedCoords[0])=="number"){ //single blocks in a group
    //remove from JSON
    console.log("deleting "+lastAddedCategory+(Object.keys(level[lastAddedCategory]).length-1).toString());
    delete level[lastAddedCategory][lastAddedCategory+(Object.keys(level[lastAddedCategory]).length-1).toString()]

    //remove from table visualization
    var cellid="#row"+lastAddedCoords[1].toString()+"col"+lastAddedCoords[0].toString();
    $(cellid).css("background-image","none");
  }
  else { //multi blocks in a group

    var p1X = lastAddedCoords[0][0];
    var p1Y = lastAddedCoords[0][1];
    var p2X = lastAddedCoords[1][0];
    var p2Y = lastAddedCoords[1][1];

    //remove from JSON
    console.log("deleting "+lastAddedCategory+(Object.keys(level[lastAddedCategory]).length-1).toString());
    delete level[lastAddedCategory][lastAddedCategory+(Object.keys(level[lastAddedCategory]).length-1).toString()]

    //remove from table visualization 
    if(lastAddedCategory==="root"){
      if(p1X==p2X){
        //delete along a column
        for(var row = Math.min(p2Y,p1Y);row <= Math.max(p2Y,p1Y);row++){
          var cellid="#row"+row.toString()+"col"+p1X.toString();
          $(cellid).css("background-image","none");
        }
      }
      else{
        //delete along a row
        for(var col = Math.min(p2X,p1X);col <= Math.max(p2X,p1X);col++){
          var cellid="#row"+p1Y.toString()+"col"+col.toString();
          $(cellid).css("background-image","none");
        }
      }
    }
    else{ //blocks
      for(var row = p1Y; row <= p2Y; row++){
        for(var col = p1X; col<=p2X; col++){
          var cellid="#row"+row.toString()+"col"+col.toString();
          $(cellid).css("background-image","none");
        }
      }
    }
  }

  
  $("#undoBtn").prop('disabled', true);
  lastAddedCategory=null;
  lastAddedCoords=null;
  outputJSON(level);
}

function check(){
    $("#check").show(0);
    $("#check").delay(600).hide(0);
}


/** Highlights JSON based on data type */
function syntaxHighlight(json) {
    json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
        var cls = 'number';
        if (/^"/.test(match)) {
            if (/:$/.test(match)) {
                cls = 'key';
            } else {
                cls = 'string';
            }
        } else if (/true|false/.test(match)) {
            cls = 'boolean';
        } else if (/null/.test(match)) {
            cls = 'null';
        }
        return '<span class="' + cls + '">' + match + '</span>';
    });
}

/** Genrates a table based on inputted game width/height*/
function tableCreate(width, height,gwidth,gheight,energy) {
  tablecreated = true;
  width=parseFloat(width);
  gwidth=parseFloat(gwidth);
  height=parseInt(height);
  gheight=parseInt(gheight);
  energy=parseInt(energy);
  if ($("#view").has("table").length > 0) {
    $("#preview").remove();
    level = {}; //starts new
  }
  var body = document.body,
    tbl = document.createElement('table');
  tbl.id = 'preview';
  for (var r = 0; r < height; r++) {
    var tr = tbl.insertRow();
    tr.id="row"+(height-r-1).toString();
    for (var c = 0; c < width; c++) {
      var td = tr.insertCell();
      td.className="gridcell";
      td.id="row"+(height-r-1).toString()+"col"+c.toString();
      td.setAttribute("row",height-r-1);
      td.setAttribute("col",c);
    }
  }
  $('#view').append(tbl);
  level["physicsSize"] = [width,height];
  level["graphicSize"] = [gwidth,gheight];
  level["gravity"] = -9.8;
  level["energy"] = energy;
  level["avatar"] = {};
  level["exit"] = {};
  level["powerup"] = {};
  level["checkpoint"] = {};
  level["enemy"] = {};
  level["platform"] = {};
  level["vine"] = {};
  level["root"] = {};
  level["breakable"] = {};
  level["Spike"] = {};
  level["RightSpike"] = {};
  level["LeftSpike"] = {};


  lastAddedCategory=null;
  lastAddedCoords=null;
  selected = null;
  $("#selectedTxt").text("Nothing");
  sw=false;
  bottomLeft=null;
  avatarplaced = false;
  goalPlaced = false;
  lastAddedCategory=null;
  lastAddedCoords=null;
  hideImgs();
  outputJSON(level);
}

/** Sets selected to be null */
function cancelSel(){
  $("#selectedTxt").text("Nothing");
  if(sw){
    var cellid="#row"+bottomLeft[1].toString()+"col"+bottomLeft[0].toString();
    $(cellid).css("background-image","none");
  }
  selected=null;
  sw=false;
  bottomLeft=null;
  hideImgs();
}

/** Updates selected element */
function select(el){
  if(!tablecreated){
    alert("Initiate level grid first.");
    return;
  }
  selected=el;
  var imgelement=$("#"+el);
  $("#selectedTxt").text(imgelement.attr("title"));
  console.log(imgelement.attr("title")+" selected");
  hideImgs();
  $("#"+el+"Img").show();

  $(".gridcell").mouseover(function() {
    $(this).css("background-color","#bdc0d8");
  }).mouseout(function() {
    $(this).css("background-color","transparent");
  });

}

/** Handles trailing images */
function hideImgs(){
  img_els.forEach(function(element){
    $("#"+element+"Img").hide();
  });
  $(".gridcell").mouseover(function() {
    $(this).css("background-color","transparent");
  })
}

/** Handles trailing images */
$(document).mousemove(function(e) {
    $('.mousefollow').offset({
        left: e.pageX,
        top: e.pageY + 20
    });
});

/** Adding elements to level visualization */
$("#view").on("click",".gridcell",function(){
  if(selected){
    var y = parseInt($(this).attr("row"));
    var x = parseInt($(this).attr("col"));

    //$(this).append("<img src="+selected+".png height='25' width='25'>");
    $(this).css("background-image","url("+selected+".png)");
    $(this).css("background-size","contain");
    $(this).css("background-repeat","no-repeat");
    $(this).css("background-position","center center");

    switch(selected){
      case "cota":
        lastAddedCategory="avatar";
        lastAddedCoords=[x,y];
        handleCota(x,y);
        break;
      case "goal":
        lastAddedCategory="exit";
        lastAddedCoords=[x,y];
        handleGoal(x,y);  
        break;
      case "pwr-up":
        lastAddedCategory="powerup";
        lastAddedCoords=[x,y];
        handlePwrUp(x,y);
        check();
        break;
      case "chkpt":
        handleCheckpt(x,y);
        check();
        break;
      case "en_fly":
        handleEnemy(x,y,0);
        check();
        break;
      case "en_grnd":
        handleEnemy(x,y,1);
        check();
        break;
      case "ungr_terr":
        handleTerrain(x,y,"ungr_terr","platform");
        break;
      case "grw_terr":
        handleTerrain(x,y,"grw_terr","vine");
        break;
      case "vine": //pregrowns
        handleTerrain(x,y,"vine","root");
        break;
      case "brk_terr": //breakable terrain
        handleTerrain(x,y,"brk_terr","breakable");
        break;
      case "rEndL":
        handleTerrain(x,y,"rEndL","LeftVineEnd");
        break;
      case "rEndR":
          handleTerrain(x,y,"rEndR","RightVineEnd");
          break;
      case "rTaperL":
          handleTerrain(x,y,"rTaperL","LeftVineTaper");
          break;
      case "rTaperR":
          handleTerrain(x,y,"rTaperR","RightVineTaper");
          break;
      case "spikeL":
          handleLeftSpike(x,y,1);
          check();
          break;
      case "spikeLrotD":
          handleLeftSpike(x,y,5);
          check();
          break;
      case "spikeLrotR":
          handleLeftSpike(x,y,3);
          check();
          break;
      case "spikeLrotL":
          handleLeftSpike(x,y,7);
          check();
          break;
      case "spikeR":
          handleRightSpike(x,y,1);
          check();
          break;
      case "spikeRrotD":
          handleRightSpike(x,y,5);
          check();
          break;
      case "spikeRrotR":
          handleRightSpike(x,y,3);
          check();
          break;
      case "spikeRrotL":
          handleRightSpike(x,y,7);
          check();
          break;
      case "spike":
          handleSpike(x,y,1);
          check();
          break;
      case "spikerotD":
          handleSpike(x,y,5);
          check();
          break;
      case "spikerotR":
          handleSpike(x,y,3);
          check();
          break;
      case "spikerotL":
          handleSpike(x,y,7);
          check();
          break;
      case "spikeDirtL":
          handleTerrain(x,y,"spikeDirtL","LeftSpikeDirt");
          break;
      case "spikeDirtLrotD":
          handleTerrain(x,y,"spikeDirtLrotD","LeftSpikeDirt");
          break;
      case "spikeDirtLrotR":
          handleTerrain(x,y,"spikeDirtLrotR","LeftSpikeDirt");
          break;
      case "spikeDirtLrotL":
          handleTerrain(x,y,"spikeDirtLrotL","LeftSpikeDirt");
          break;
      case "spikeDirtR":
          handleTerrain(x,y,"spikeDirtR","RightSpikeDirt");
          break;
      case "spikeDirtRrotD":
          handleTerrain(x,y,"spikeDirtRrotD","RightSpikeDirt");
          break;
      case "spikeDirtRrotR":
          handleTerrain(x,y,"spikeDirtRrotR","RightSpikeDirt");
          break;
      case "spikeDirtRrotL":
          handleTerrain(x,y,"spikeDirtRrotL","RightSpikeDirt");
          break;
      case "spikeDirt":
          handleTerrain(x,y,"spikeDirt","SpikeDirt");
          break;
      case "spikeDirtrotD":
          handleTerrain(x,y,"spikeDirtrotD","SpikeDirt");
          break;
      case "spikeDirtrotL":
          handleTerrain(x,y,"spikeDirtrotL","SpikeDirt");
          break;
      case "spikeDirtrotR":
          handleTerrain(x,y,"spikeDirtrotR","SpikeDirt");
          break;
      case "topright":
          handleTerrain(x,y,"topright","topright");
          break;
      case "bottomright":
          handleTerrain(x,y,"bottomright","bottomright");
          break;
      case "topleft":
          handleTerrain(x,y,"topleft","topleft");
          break;
      case "bottomleft":
          handleTerrain(x,y,"bottomleft","bottomleft");
          break;
      case "edgeU":
          handleTerrain(x,y,"edgeU","edgeU");
          break;
      case "edgeD":
          handleTerrain(x,y,"edgeD","edgeD");
          break;
      case "edgeL":
          handleTerrain(x,y,"edgeL","edgeL");
          break;
      case "edgeR":
          handleTerrain(x,y,"edgeR","edgeR");
          break;
      case "breakablebottom":
          handleTerrain(x,y,"breakablebottom","breakablebottom");
          break;
      case "breakableleft":
          handleTerrain(x,y,"breakableleft","breakableleft");
          break;
      case "breakableright":
          handleTerrain(x,y,"breakableright","breakableright");
          break;
      case "breakabletop":
          handleTerrain(x,y,"breakabletop","breakabletop");
          break;
      case "breakablebottomright":
          handleTerrain(x,y,"breakablebottomright","breakablebottomright");
          break;
      case "breakablebottomleft":
          handleTerrain(x,y,"breakablebottomleft","breakablebottomleft");
          break;
      case "breakabletopright":
          handleTerrain(x,y,"breakabletopright","breakabletopright");
          break;
      case "breakabletopleft":
          handleTerrain(x,y,"breakabletopleft","breakabletopleft");
          break;
      case "breakableedgethreedown":
          handleTerrain(x,y,"breakableedgethreedown","breakableedgethreedown");
          break;
      case "breakableedgethreeright":
          handleTerrain(x,y,"breakableedgethreeright","breakableedgethreeright");
          break;
      case "breakableedgethreeleft":
          handleTerrain(x,y,"breakableedgethreeleft","breakableedgethreeleft");
          break;
      case "breakableedgethreeup":
          handleTerrain(x,y,"breakableedgethreeup","breakableedgethreeup");
          break;
      case "breakabletopdown":
          handleTerrain(x,y,"breakabletopdown","breakabletopdown");
          break;
      case "breakableleftright":
          handleTerrain(x,y,"breakableleftright","breakableleftright");
          break;
      default:
        break;
    }
  }
});

function handleRightSpike(x,y,dir){
  lastAddedCategory="RightSpike";
  lastAddedCoords=[x,y];
  level["RightSpike"]["RightSpike"+(Object.keys(level["RightSpike"]).length).toString()] =
  {
    "points" :  ["0.0f", "0.53125f", "0.09375f", "0.40625f", "0.1875f", "0.75f", "0.34375f", "0.46875f", "0.4375f",
                "0.65625f", "0.53125f", "0.34375f", "0.6875f", "0.65625f", "0.84375f", "0.25f", "0.96875f", "0.40625f",
                "0.96875f", "0.0f", "0.0f", "0.0f"
            ],
    "pos":           [ x,  y],
    "texture": "RightSpike",
    "direction":dir
  }
  outputJSON(level);
}

function handleLeftSpike(x,y,dir){
  lastAddedCategory="LeftSpike";
  lastAddedCoords=[x,y];
  level["LeftSpike"]["LeftSpike"+(Object.keys(level["LeftSpike"]).length).toString()] =
  {
    "points": [
                "0.03968f", "0.3968f", "0.2368f", "0.219968f", "0.2368f", "0.579968f", "0.419968f", "0.0f", "0.499968f", "0.739968f", "0.659968f", "0.43968f", "0.67968f", "0.739968f", "0.939968f", "0.299968f", "0.99968f", "0.419968f", "0.99968f", "0.0f", "0.03968f", "0.0f"
            ],
    "pos":           [ x,  y],
    "texture": "LeftSpike",
    "direction":dir
  }
  outputJSON(level);
}

function handleSpike(x,y,dir){
  lastAddedCategory="Spike";
  lastAddedCoords=[x,y];
      level["Spike"]["Spike"+(Object.keys(level["Spike"]).length).toString()] =
  {
    "points": [
                "0.0f", "0.53125f", "0.09375f", "0.40625f", "0.1875f", "0.75f", "0.34375f", "0.46875f", "0.4375f", "0.65625f",
                "0.53125f", "0.34375f", "0.6875f", "0.65625f", "0.84375f", "0.25f", "1.0f", "0.40625f", "1.0f", "0.0f", "0.0f", "0.0f"
            ],
    "pos":           [ x,  y],
    "texture": "Spike",
    "direction":dir
  }
  outputJSON(level);
}


function handleEnemy(x,y,enemyType){
  lastAddedCategory="enemy";
  lastAddedCoords=[x,y];
  level["enemy"]["enemy"+(Object.keys(level["enemy"]).length).toString()] =
  {
        "pos":           [ x,  y],
        "size":          [ 0.45, 0.61],
        "bodytype":      "dynamic",
        "density":        1.0,
        "friction":       0.0,
        "restitution":    0.0,
        "type":       enemyType,
        "sensorsize":    [ 0.183, 0.05 ],
        "sensorname":    "enemySensor"
    };
  outputJSON(level);
  sw=false;
  bottomLeft=null;
}


function handleTerrain(x,y,terrainImg,terrainType){
  if (sw){
    //set second corner
    //fill in table
    if (terrainType != "root"){ //not pregrown roots - fill in everything from bottom left to top right
      var platHeight=y-bottomLeft[1]+1;
      var platWidth=x-bottomLeft[0]+1;

      if (platHeight<=0 || platWidth<=0){ //INCORRECT PLACEMENT
        alert("Invalid placement.");
        var cellid="#row"+bottomLeft[1].toString()+"col"+bottomLeft[0].toString();
        $(cellid).css("background-image","none");
        var cellid="#row"+y.toString()+"col"+x.toString();
        $(cellid).css("background-image","none");
        sw=false;
        bottomLeft=null;
        return; //SHORT CIRCUITS
      }

      var x1 = "0.0f";
      var y1 = "0.0f";

      var x2 = "0.0f";
      var y2 = platHeight+".0f";

      var x3 = platWidth+".0f";
      var y3 = platHeight+".0f";

      var x4 = platWidth+".0f";
      var y4 = "0.0f";

      lastAddedCoords=[[bottomLeft[0],bottomLeft[1]],[x,y]];

      var xcenter = bottomLeft[0] + platWidth/2;
      var ycenter = bottomLeft[1] + platHeight/2;

      for(var r=bottomLeft[1];r<bottomLeft[1]+platHeight;r++){
        for(var c=bottomLeft[0];c<bottomLeft[0]+platWidth;c++){
          var cellid="#row"+r.toString()+"col"+c.toString();
          $(cellid).css("background-image","url("+terrainImg+".png)");
          $(cellid).css("background-size","contain");
          $(cellid).css("background-repeat","no-repeat");
          $(cellid).css("background-position","center center");
        }
      }
    }
    else{ //pregrown roots
      var firstClickX=bottomLeft[0];
      var firstClickY=bottomLeft[1];
      var secondClickX=x;
      var secondClickY=y;
      var length;
      var direction;
      lastAddedCoords=[[bottomLeft[0],bottomLeft[1]],[x,y]];

      if(secondClickX>firstClickX && secondClickY==firstClickY){ //  ----> right
        length = secondClickX-firstClickX+1;
        direction = 3;
        for(var c=firstClickX;c<firstClickX+length;c++){
          var cellid="#row"+firstClickY+"col"+c.toString(); //add columns
          if (c==firstClickX){
            $(cellid).css("background-image","url("+terrainImg+"anchorright.png)");
          }
          else{
            $(cellid).css("background-image","url("+terrainImg+".png)");
          }
          
          $(cellid).css("background-size","contain");
          $(cellid).css("background-repeat","no-repeat");
          $(cellid).css("background-position","center center");
          }
      }
      
      else if(secondClickX<firstClickX && secondClickY==firstClickY){ //  <---- left
        length = firstClickX-secondClickX+1;
        direction = 7;
        for(var c=firstClickX;c>firstClickX-length;c--){ //add columns
          var cellid="#row"+firstClickY+"col"+c.toString();
          if (c==firstClickX){
            $(cellid).css("background-image","url("+terrainImg+"anchorleft.png)");
          }
          else{
            $(cellid).css("background-image","url("+terrainImg+".png)");
          }
          $(cellid).css("background-size","contain");
          $(cellid).css("background-repeat","no-repeat");
          $(cellid).css("background-position","center center");
        }
      }
      else if(secondClickY>firstClickY && secondClickX==firstClickX){ // up
        length = secondClickY - firstClickY+1;
        direction = 1;
        for(var r=firstClickY;r<firstClickY+length;r++){ //add rows
          var cellid="#row"+r.toString()+"col"+firstClickX.toString();
          if(r==firstClickY){
            $(cellid).css("background-image","url("+terrainImg+"anchorup.png)");
          }
          else{
            $(cellid).css("background-image","url("+terrainImg+"Rotated.png)");
          }
        
          $(cellid).css("background-size","contain");
          $(cellid).css("background-repeat","no-repeat");
          $(cellid).css("background-position","center center");
        }
      }
      else if (secondClickY<firstClickY && secondClickX==firstClickX){ //down
        length = firstClickY - secondClickY+1;
        direction = 5;
        for(var r=firstClickY;r>firstClickY-length;r--){ //add rows
          var cellid="#row"+r.toString()+"col"+firstClickX.toString();
          if(r==firstClickY){
            $(cellid).css("background-image","url("+terrainImg+"anchordown.png)");
          }
          else{
            $(cellid).css("background-image","url("+terrainImg+"Rotated.png)");
          }
          $(cellid).css("background-size","contain");
          $(cellid).css("background-repeat","no-repeat");
          $(cellid).css("background-position","center center");
        }
      }
      else{
        alert("Invalid placement.");
        var cellid="#row"+bottomLeft[1].toString()+"col"+bottomLeft[0].toString();
        $(cellid).css("background-image","none");
        var cellid="#row"+y.toString()+"col"+x.toString();
        $(cellid).css("background-image","none");
        sw=false;
        bottomLeft=null;
        return;
      }
      lastAddedCategory="root";
      level["root"]["root"+(Object.keys(level["root"]).length).toString()] =
        {
          "pos":           [firstClickX,firstClickY],
          "direction":       direction,
          "length" :         length
        };
        check();
    }
    if(terrainType=="LeftVineEnd"||terrainType=="RightVineEnd"||terrainType=="LeftVineTaper"||terrainType=="RightVineTaper"){
      lastAddedCategory="vine";
      level["vine"]["vine"+(Object.keys(level["vine"]).length).toString()] =
        {
          "points":           [x1,y1,x2,y2,x3,y3,x4,y4],
          "pos":              [bottomLeft[0],bottomLeft[1]],
          "texture" :         terrainType
        };
        check();
    }
    else if (terrainType.includes("breakable")){ //all breakables
      lastAddedCategory="breakable";
      level["breakable"]["breakable"+(Object.keys(level["breakable"]).length).toString()] =
        {
          "points":           [x1,y1,x2,y2,x3,y3,x4,y4],
          "pos":              [bottomLeft[0],bottomLeft[1]],
          "texture" :         terrainType
        };
        check();

    }
    else if(terrainType.toUpperCase().includes("SPIKEDIRT")){
      
      if(!terrainImg.includes("rot")){
        lastAddedCategory="platform";
        level["platform"]["platform"+(Object.keys(level["platform"]).length).toString()] =
          {
            "points":           [x1,y1,x2,y2,x3,y3,x4,y4],
            "pos":              [bottomLeft[0],bottomLeft[1]],
            "texture" :         terrainType,
            "rotation" :        1
          };
          check();
      }
      else{
        var rot;
        var dir = terrainImg.charAt(terrainImg.length-1);
        if(dir==="R"){
          rot = 3;
        }
        else if(dir==="D"){
          rot = 5;
        }
        else if(dir =="L"){
          rot = 7;
        }
        else{
          alert("Something went wrong.");
        }
        lastAddedCategory="platform";
        level["platform"]["platform"+(Object.keys(level["platform"]).length).toString()] =
          {
            "points":           [x1,y1,x2,y2,x3,y3,x4,y4],
            "pos":              [bottomLeft[0],bottomLeft[1]],
            "texture" :         terrainType,
            "rotation" :        rot
          };
          check();
      }
    }
    else if(terrainType=="topright"||terrainType=="topleft"||terrainType=="bottomright"||terrainType=="bottomleft"||terrainType=="edgeR"||terrainType=="edgeD"||terrainType=="edgeU"||terrainType=="edgeL"){
      lastAddedCategory="platform";
      level["platform"]["platform"+(Object.keys(level["platform"]).length).toString()] =
        {
          "points":           [x1,y1,x2,y2,x3,y3,x4,y4],
          "pos":              [bottomLeft[0],bottomLeft[1]],
          "texture" :         terrainType
        };
        check();
    }
    else if (terrainType!="root"){
      lastAddedCategory=terrainType;
      level[terrainType][terrainType+(Object.keys(level[terrainType]).length).toString()] =
        {
          "points":           [x1,y1,x2,y2,x3,y3,x4,y4],
          "pos":              [bottomLeft[0],bottomLeft[1]],
          "texture" :         terrainType
        };
        check();
    }
    //fill in json


    sw=false;
    bottomLeft=null;
    outputJSON(level);
  }
  else{
    //set first corner
    var cellid="#row"+y.toString()+"col"+x.toString();
    $(cellid).css("background","linear-gradient(rgba(255,69,0,0.5), rgba(255,69,0,0.5)),rgba(0,0,0,0) url('"+terrainImg+".png')");
    $(cellid).css("background-size","contain");
    $(cellid).css("background-repeat","no-repeat");
    $(cellid).css("background-position","center center");
    sw=true;
    bottomLeft=[x,y];
  }
}

function handleCheckpt(x,y){
  level["checkpoint"]["checkpoint"+(Object.keys(level["checkpoint"]).length).toString()] =
  {
        "pos":           [ x, y],
        "size":          [ 1.0, 1.0],
        "bodytype":      "static",
        "density":        0.0,
        "friction":       0.0,
        "restitution":    0.0,
        "texture":       "checkpoint"
    };
  outputJSON(level);
  sw=false;
  bottomLeft=null;
}

function handlePwrUp(x,y){
  
  level["powerup"]["powerup"+(Object.keys(level["powerup"]).length).toString()] =
  {
        "points":           ["0.0f","0.0f","0.0f","1.0f","1.0f","1.0f","1.0f","0.0f"],
        "pos" : [x,y],
        "texture":       "powerup"
    };
  outputJSON(level);
  sw=false;
  bottomLeft=null;
}


function handleGoal(x,y){
  if(goalPlaced){
    alert("Already placed goal");
    var cellid="#row"+y.toString()+"col"+x.toString();
    $(cellid).css("background-image","none");
    return;
  }
  level["exit"] =
  {
        "points":           ["0.0f","0.0f","0.0f","1.0f","1.0f","1.0f","1.0f","0.0f"],
        "pos" : [x,y],
        "texture":       "exit"
    };
  outputJSON(level);
  goalPlaced = true;
  check();
}

function handleCota(x,y){
  if(avatarplaced){
    alert("Already placed Cota");
    var cellid="#row"+y.toString()+"col"+x.toString();
    $(cellid).css("background-image","none");
    return;
  }
  level["avatar"] =
  {
        "pos":           [ x,  y],
        "size":          [ 0.45, 0.61],
        "bodytype":      "dynamic",
        "density":        1.0,
        "friction":       0.0,
        "restitution":    0.0,
        "texture":       ["dude"],
        "sensorsize":    [ 0.183, 0.05 ],
        "sensorname":    "dudeGroundSensor"
    };

  outputJSON(level);
  avatarplaced = true;
  check();
}


function exportToJsonFile() {
    let dataStr = JSON.stringify(level,undefined,4);
    let dataUri = 'data:application/json;charset=utf-8,'+ encodeURIComponent(dataStr);

    let exportFileDefaultName = 'level.json';

    let linkElement = document.createElement('a');
    linkElement.setAttribute('href', dataUri);
    linkElement.setAttribute('download', exportFileDefaultName);
    linkElement.click();
}
