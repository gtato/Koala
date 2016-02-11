graph = {}
boots =[]
nodes =[]
$( document ).ready(function() {
   add_listeners()   
   graph = new myGraph("#hello"); 
   updateGraph();
});

function add_listeners(){
    $(document).on('click', '#add_node', add_node);
    $(document).on('click', '#delete_all_nodes', delete_all_nodes);
    
}



function add_node(){


    var id = get_node_id()
    var boot_id = $("#boot_node_id").val()

    $( "#console" ).append( "> Adding node: " + id + " using node: " + boot_id + "<br>" );
    $("#cons_cont").animate({ scrollTop: $('#cons_cont').height()}, 1000);

    $.getJSON("/add_node", {
          node_id: id,
          boot_node_id: boot_id
        },
        function(data, status){
            updateGraph()
            $("#node_id").val('')
//            $("#node_id").val('1')
        });
}

function get_node_id(){
    var dc = $("#dc_id").val()
    if (dc.length == 0)
        dc = random('dc')
    var nid = $("#node_id").val()
    if (nid.length == 0)
        nid = random('n')

    var id = dc + "-" + nid

    exists = false;
    for(var i = 0; i < nodes.length; i++){
        if(nodes[i].id == id){
            exists = true
            break
        }
    }

    if (exists)
        return get_node_id()
    return id
}

function random(what){
    dc_ids = ['a', 'b','c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z']
    if (what == 'dc')
        return dc_ids[Math.floor((Math.random() * dc_ids.length) )];
    if (what == 'n')
        return String(Math.floor((Math.random() * 100)));
    if (what == 'b'){
        if (boots.length == 0)
            return '';
        return boots[Math.floor((Math.random() * boots.length) )];
    }
}

function delete_all_nodes(){
    $.getJSON("/delete_nodes", 
        function(data, status){
            // updateGraph()
            location.reload(); 
        });
}


function updateGraph(){
    $.getJSON("/get_nodes", function(data, status){
        nodes = data.result

        var dcs = [];
        $('#boot_node_id').empty();
        boots = [];
        for(var i = 0; i < nodes.length; i++){
            $('#boot_node_id').append('<option value="'+nodes[i].id+'">'+nodes[i].id+'</option>');
            boots.push(nodes[i].id)
            dcs.push(nodes[i].dc_id)
        }

        dcs = uniq_fast(dcs)
        for(var i = 0; i < dcs.length; i++){
            $('#dcs').html('<option value="'+dcs[i]+'">'+dcs[i]+'</option>');
        }
        graph.addNodesAndLinks(nodes);
    });
}


function mouseover(d) {
    
    html = '<div style="text-align:center; padding:5px" ><strong>' + d.id +'</strong> <br/><hr>'
    table = false;
    
    if (d.rt.locals.predecessor || d.rt.locals.successor){
        html += '<br><span>Local</span> <br/>'
        html += '<table border="1" style="width:100%"><tr><th>role</th><th>id</th><th>latency</th></tr>'
        table = true;
    }


    if (d.rt.locals.predecessor)
        html += '<tr><td>p</td><td>'+d.rt.locals.predecessor.id+'</td><td>'+d.rt.locals.predecessor.latency+'</td></tr>'
    if (d.rt.locals.successor)
        html += '<tr><td>s</td><td>'+d.rt.locals.successor.id+'</td><td>'+d.rt.locals.successor.latency+'</td></tr>'


    if (table){
        html += '</table>'    
        table = false
    }


    if (d.rt.globals.predecessor || d.rt.globals.successor){
        html += '<br><span>Global</span> <br/>'
        html += '<table border="1" style="width:100%"><tr><th>role</th><th>id</th><th>latency</th></tr>'
        table = true;
    }


    if (d.rt.globals.predecessor)
        html += '<tr><td>p</td><td>'+d.rt.globals.predecessor.id+'</td><td>'+d.rt.globals.predecessor.latency+'</td></tr>'
    if (d.rt.globals.successor)
        html += '<tr><td>s</td><td>'+d.rt.globals.successor.id+'</td><td>'+d.rt.globals.successor.latency+'</td></tr>'

    if (table)
        html += '</table>'    


    if (d.rt.locals.visited.length > 0 ){
        html += '<br><span>Local visited</span> <br/>'
        html += '<table border="1" style="width:100%"><tr><th>id</th><th>latency</th></tr>'

        for(i = 0; i < d.rt.locals.visited.length; i++)
            html += '<tr><td>'+d.rt.locals.visited[i].id+'</td><td>'+d.rt.locals.visited[i].latency+'</td></tr>'

        html += '</table>'
    }


    if (d.rt.globals.visited.length > 0 ){
        html += '<br><span>Global visited</span> <br/>'
        html += '<table border="1" style="width:100%"><tr><th>id</th><th>latency</th></tr>'

        for(i = 0; i < d.rt.globals.visited.length; i++)
            html += '<tr><td>'+d.rt.globals.visited[i].id+'</td><td>'+d.rt.globals.visited[i].latency+'</td></tr>'
        html += '</table>'
    }




    html += '</div>'
    
    d3.select("#node_info").html(html);
  
}

function mouseout() {
  d3.select("#node_info").html('');
}


function uniq_fast(a) {
    var seen = {};
    var out = [];
    var len = a.length;
    var j = 0;
    for(var i = 0; i < len; i++) {
         var item = a[i];
         if(seen[item] !== 1) {
               seen[item] = 1;
               out[j++] = item;
         }
    }
    return out;
}

function myGraph(el) {
    var color = d3.scale.category20();

    this.addNodesAndLinks = function (snodes){
        llinks = []
        new_nodes = []
        present = false;
        for(var i = 0; i < snodes.length; i++){
            new_nodes.push({"id":snodes[i].id, "group":snodes[i].dc_id, 'rt':snodes[i].rt});

            var all = [];

            all_ns = [snodes[i].rt.locals.predecessor, snodes[i].rt.locals.successor, snodes[i].rt.globals.predecessor, snodes[i].rt.globals.successor]
            all = all_ns.filter(function(value){ return value!=null })

            for(var j = 0; j < all.length; j++){
                var n = snodes[i].id.localeCompare(all[j].id);

                if (n > 0)
                    lid = snodes[i].id + '|'  + all[j].id
                else
                    lid = all[j].id + '|' + snodes[i].id

                llinks.push(lid)
            }
        }


        llinks = uniq_fast(llinks)

        var new_links = [];

        for(var i = 0; i < llinks.length; i++){
            var res = llinks[i].split("|");
            new_links.push({"source": findNode(res[0], new_nodes) , "target": findNode(res[1], new_nodes) });

        }
        update(new_nodes, new_links);
    }

    var findNode = function (id, nodes) {
        for (var i=0; i < nodes.length; i++) {
            if (nodes[i].id === id)
                return nodes[i]
        };
    }


    // set up the D3 visualisation in the specified element
    var w = $(el).innerWidth(),
        h = $(el).innerHeight();

    var vis = this.vis = d3.select(el).append("svg:svg")
        .attr("width", w)
        .attr("height", h);

    var force = d3.layout.force()
        .gravity(.05)
        .distance(100)
        .charge(-300)
        // .charge(-50)

        .size([w, h]);

    var update = function (nodes, links) {

        force.nodes(nodes);
        
        force.links(links);


        var link = vis.selectAll("line.link")
            .data(force.links(), function(d) { return d.source.id + "-" + d.target.id; });

        link.enter().insert("line")
            .attr("class", "link");
            

        link.exit().remove();

        var node = vis.selectAll("g.node")
            .data(nodes, function(d) { return d.id;});

        var nodeEnter = node.enter().append("g")
            .attr("class", "node")
            .on("mouseover", mouseover)
            .on("mouseout", mouseout)
            .call(force.drag);

        node.append("circle")
        .style("fill", function(d) { return color(d.group);})
        .attr("r", 8);
    
        nodeEnter.append("text")
            .attr("class", "nodetext")
            .attr("dx", 12)
            .attr("dy", ".35em")
            .text(function(d) {return d.id});

        node.exit().remove();

        force.linkDistance(function(d) { if (d.source.group == d.target.group) return 30; else return 100;  });
        force.on("tick", function() {
          
          link.attr("x1", function(d) { 
                            return d.source.x; })
              .attr("y1", function(d) { 
                            return d.source.y; })
              .attr("x2", function(d) { 
                            return d.target.x; })
              .attr("y2", function(d) { 
                            return d.target.y; });

          node.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
          
        });

        // Restart the force layout.
        force.start();

        var k = 0;
        while ((force.alpha() > 1e-2) && (k < 150)) {
            force.tick(),
            k = k + 1;
        }
    }

    // Make it all go
    update([],[]);
}



