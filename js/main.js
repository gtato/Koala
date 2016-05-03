graph = {}
boots =[]
nodes =[]
gmode='readonly'
$( document ).ready(function() {
   add_listeners()   
   graph = new myGraph("#hello"); 
   get_nodes();
});

function add_listeners(){
    $(document).on('click', '#add_node', add_node);
    $(document).on('click', '#toggle_list', toggle_list);

    $(document).on('click', '#add_list', add_list);
    $(document).on('click', '#exec_list', exec_list);
    $(document).on('change', '#add_list_select', show_list);
    $(document).on('click', '#delete_lists', delete_lists);

    $(document).on('click', '#toggle_route_list', toggle_route_list);
    $(document).on('click', '#route', route);
    $(document).on('click', '#add_route_list', add_route_list);
    $(document).on('click', '#swap', swap);
    $(document).on('click', '#add_all_route_list', add_all_route_list);
    $(document).on('click', '#exec_route_list', exec_route_list);
    $(document).on('change', '#add_route_list_select', show_route_list);
    $(document).on('click', '#delete_route_lists', delete_route_lists);

    $(document).on('click', '#uploadchanges', upload_changes);
    $(document).on('click', '#save', save);
    $(document).on('click', '#restore', restore);

    $(document).on('click', '#delete_all_nodes', delete_all_nodes);
    $(document).on('mouseover', '#mapping', show_mapping);
    $(document).on('mouseleave', '#mapping', hide_mapping);

    $(document).on('click', '#expand_log', toggle_log);

}

function toggle_list(e){
    if($(e.target).html().indexOf('Show') > -1)
    {
        $("#random_list").show()
        $(e.target).html('Hide list options')
    }else{
        $("#random_list").hide()
        $(e.target).html('Show list options')
    }
}

function toggle_log(e){
    if($(e.target).html().indexOf('more') > -1)
    {
//        $("#random_list").show()
        $(e.target).html('Show less...')
        $('#cons_cont').css('height', '300px')
    }else{
//        $("#random_list").hide()
        $(e.target).html('Show more...')
        $('#cons_cont').css('height', '100px')
    }
}

function toggle_route_list(e){
    if($(e.target).html().indexOf('Show') > -1)
    {
        $("#random_route_list").show()
        $(e.target).html('Hide route list options')
    }else{
        $("#random_route_list").hide()
        $(e.target).html('Show route list options')
    }
}


function add_all_route_list(){
    if (nodes.length == 0){
        alert('Add some nodes first!')
        return
    }

    routes = []
    for(var i=0; i < nodes.length; i++){
        src = nodes[i];
        slpid = src.rt.locals.predecessor == null ? '' : src.rt.locals.predecessor.id
        slsid = src.rt.locals.successor== null ? '' : src.rt.locals.successor.id
        sgpid = src.rt.globals.predecessor == null ? '' : src.rt.globals.predecessor.id
        sgsid = src.rt.globals.successor== null ? '' : src.rt.globals.successor.id
        for(var j=0; j < nodes.length; j++){
            dest = nodes[j];
            dlpid = dest.rt.locals.predecessor == null ? '' : dest.rt.locals.predecessor.id
            dlsid = dest.rt.locals.successor== null ? '' : dest.rt.locals.successor.id
            dgpid = dest.rt.globals.predecessor == null ? '' : dest.rt.globals.predecessor.id
            dgsid = dest.rt.globals.successor== null ? '' : dest.rt.globals.successor.id



            if (src.id != dest.id
                && slpid != dest.id && slsid != dest.id
                && sgpid != dest.id && sgsid != dest.id
                && dlpid != src.id && dlsid != dest.id
                && dgpid != src.id && dgsid != dest.id
                )
            {
//                routes.push({s:src.id, d:dest.id})
                route = src.id+" -> "+dest.id;
                routes.push(route)
            }
        }
    }

    if (routes.length == 0){
        alert('No relevant paths were detected!')
        return
    }

    name = 'N-'+routes.length+'-ALL'
    val = routes.toString()
    $('#add_route_list_select').append('<option value="'+val+'">'+name+'</option>');
    show_list_info(name, val)

    store_list_to_ls("rlist", name, val)
//    for(var i = 0; i < routes.length; i++){
//        animate = i == routes.length -1 ? true : false;
//        log(routes[i].s+" -> "+routes[i].d, animate)
//    }
}

function add_list(){
    var nr = $("#nr_add_nodes").val()
    if (nr.length == 0){
        alert('Please select the number of nodes to generate!')
        return
    }

    old_dc = $("#dc_id").val()
    old_nid = $("#node_id").val()

    $("#dc_id").val('')
    $("#node_id").val('')

    list = []
    id_list = []
    for(var i=0; i<nr; i++){
        if (list.length == 0)
            boot_id = ''
        else
            boot_id = id_list[Math.floor((Math.random() * id_list.length))]

        nid = get_node_id()
        ndes = nid+" | "+boot_id;

        id_list.push(nid)
        list.push(ndes )
    }
    name = 'N-'+nr+'-'+rand_str(3)
    val = list.toString()
    $('#add_list_select').append('<option value="'+val+'">'+name+'</option>');
    show_list_info(name, val)

    $("#dc_id").val(old_dc)
    $("#node_id").val(old_nid)

    store_list_to_ls("nlist", name, val)

}

function exec_list(){
    lnodes = $("#add_list_select").val();
    if(lnodes == null){
        alert("Please add a list first!")
        return
    }
    lname = $("#add_list_select option:selected").text();
    $.getJSON("add_list", {
         nodes: lnodes
         },
         function(data, status){
            msgs = data.msgs;
            nodes = data.result
            update_graph(nodes)
            log("Added list: " + lname, msgs)
        }
    );
}

function show_list(){
    txt = $("#add_list_select option:selected").text();
    val = $("#add_list_select").val()
    show_list_info(txt, val)
}

function delete_lists(){
    localStorage.removeItem("nlist");
    $('#add_list_select').empty();
    $("#node_info").html("");
}

function add_route_list(){
    var nr = $("#nr_routs").val()
    if (nr.length == 0){
        alert('Select the number of routes to generate!')
        return
    }
    if (nodes.length == 0){
        alert('Add some nodes first!')
        return
    }


    list = []
    id_list = []
    for(var i=0; i<nr; i++){
        fr = nodes[Math.floor((Math.random() * nodes.length))]
        to = nodes[Math.floor((Math.random() * nodes.length))]
        route = fr.id+" -> "+to.id;
        list.push(route)
    }
    name = 'R-'+nr+'-'+rand_str(3)
    val = list.toString()
    $('#add_route_list_select').append('<option value="'+val+'">'+name+'</option>');
    show_list_info(name, val)


    store_list_to_ls("rlist", name, val)

}

function exec_route_list(){
    lroutes = $("#add_route_list_select").val();
    if(lroutes == null){
        alert("Add a route list first!")
        return
    }
    lname = $("#add_route_list_select option:selected").text();
    $.getJSON("route_list", {
         routes: lroutes
         },
         function(data, status){
            msgs = data.msgs;
            paths = data.result
            log("Executed route list: " + lname, msgs)
            for(var i=0; i < paths.length; i++){
                path = paths[i]
                animate = i == paths.length -1 ? true : false;
                log("Path from " + path[0] + " to " + path[path.length -1]+ ": ["+ path.toString()+"]", [], animate)
            }

        }
    );
}

function show_route_list(){
    txt = $("#add_route_list_select option:selected").text();
    val = $("#add_route_list_select").val()
    show_list_info(txt, val)
}

function delete_route_lists(){
    localStorage.removeItem("rlist");
    $('#add_route_list_select').empty();
    $("#node_info").html("");
}

function show_list_info(text, value){
    list = value.split(',')
    txt = '<div style="text-align:center; padding:5px" ><strong>' + text+ '</strong><hr><br>';
    for(var i =0; i < list.length; i++){
        txt += list[i]+'<br>'
    }
    txt +='</div>'
    $("#node_info").html(txt);
}


function swap(){
    var from = $("#route_from").val()
    var to  = $("#route_to").val()

    $("#route_from").val(to)
    $("#route_to").val(from)
}

function upload_changes(){
    $.getJSON("upload_changes", {
          nodes: JSON.stringify(nodes)
        },
        function(data, status){
//            msgs = data.msgs;
//            log("Adding node: " + id + " using node: " + boot_id, msgs)
//            get_nodes()
//            $("#node_id").val('')
            alert('nodes updated')
        });
}

function save(){
  $.getJSON("get_nodes", function(data, status){
      nodes = data.result
      text = JSON.stringify(nodes)
      filename = 'nodes.js'
      var element = document.createElement('a');
      element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(text));
      element.setAttribute('download', filename);
      element.style.display = 'none';
      document.body.appendChild(element);

      element.click();

      document.body.removeChild(element);
  });

}


function restore(){
   if ($('#nodes_file')[0].files.length == 0){
    alert('Please specify the file with nodes to restore!')
    return;
   }

   var nodes_file = $('#nodes_file')[0].files[0];
   var formData = new FormData();
   formData.append('file', nodes_file, nodes_file.name);

   $.ajax({
    url : "/restore",
    type: "POST",
    data : formData,
    processData: false,
    contentType: false,
    success:function(data, textStatus, jqXHR){
        nodes = data.result;
        update_graph(nodes);
    },
    error: function(jqXHR, textStatus, errorThrown){
        //if fails
    }
});

//   alert(nodes_file.name)
}

function route(){
    var from = $("#route_from").val()
    var to  = $("#route_to").val()

    if (from == null){
        alert('Add some nodes first!')
        return
    }

    $.getJSON("route", {
          from: from,
          to: to
        },
        function(data, status){
            path = data.result;
            msgs = data.msgs;
            log("Path from " + from + " to " + to + ": ["+ path.toString()+"] ", msgs )
        });
}

function log(str, msgs, animate){
    if (animate === undefined)
        animate = true;

    ltext = "> " + str
    if (msgs.length > 0)
        ltext+= ', msgs: ' + msgs[0] + ' ('+ msgs[1] + ' inter, ' + msgs[2] +' intra)'
    ltext +=  "<br>"
    $( "#console" ).append( ltext );
    if(animate)
        $("#cons_cont").animate({ scrollTop: $('#cons_cont').height()+999999}, 1000);
}

function add_node(){


    var id = get_node_id()
    var boot_id = $("#boot_node_id").val()


    $.getJSON("add_node", {
          node_id: id,
          boot_node_id: boot_id
        },
        function(data, status){
            msgs = data.msgs;
            log("Adding node: " + id + " using node: " + boot_id, msgs)
            get_nodes()
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
    $.getJSON("delete_nodes",
        function(data, status){
            location.reload(); 
        });
}


function get_nodes(){
    $.getJSON("get_nodes", function(data, status){
        nodes = data.result
        update_graph(nodes)
    });
}

function update_graph(nodes){
    var dcs = [];
    $('#boot_node_id').empty();
    $('#route_from').empty();
    $('#route_to').empty();
    $('#dcs').empty();

    $('#add_list_select').empty();
    $('#add_route_list_select').empty();

    boots = [];
    for(var i = 0; i < nodes.length; i++){
        boots.push(nodes[i].id)
        dcs.push(nodes[i].dc_id)
    }

    boots.sort()
    for(var i = 0; i < boots.length; i++){
        $('#boot_node_id').append('<option value="'+boots[i]+'">'+boots[i]+'</option>');
        $('#route_from').append('<option value="'+boots[i]+'">'+boots[i]+'</option>');
        $('#route_to').append('<option value="'+boots[i]+'">'+boots[i]+'</option>');
    }

    dcs = uniq_fast(dcs)
    dcs.sort()
    for(var i = 0; i < dcs.length; i++){
        $('#dcs').append('<option value="'+dcs[i]+'">'+dcs[i]+'</option>');
    }

    nlist = localStorage.getItem("nlist");
    if(nlist !== null){
        nlist = JSON.parse(nlist)
        for(var i = 0; i < nlist.length; i++){
            $('#add_list_select').append('<option value="'+nlist[i].v+'">'+nlist[i].n+'</option>');
        }
    }


    rlist = localStorage.getItem("rlist");
    if(rlist !== null){
        rlist = JSON.parse(rlist)
        for(var i = 0; i < rlist.length; i++){
            $('#add_route_list_select').append('<option value="'+rlist[i].v+'">'+rlist[i].n+'</option>');
        }
    }

//    total_diff = 0
//    for(var i=0; i < nodes.length; i++){
//        total_diff += Math.abs( nodes[i].nr_local_nodes - nodes.length);
//    }
//    mae = Math.round(total_diff/nodes.length * 100) / 100 ;

    $('#general_info').html('Total nr. nodes: ' + nodes.length  )


    graph.addNodesAndLinks(nodes);
}

function store_list_to_ls(listid, name, id){
    list = localStorage.getItem(listid)
    if(list === null)
        list = []
    else
        list = JSON.parse(list)
    list.push({n:name,v:val})
    localStorage.setItem(listid, JSON.stringify(list));
}



function show_node_info(d, mode){
    colors = ['red','orange', 'blue', 'green']

    html = '<div style="text-align:center; padding:5px" ><strong>' + d.id + '</strong><br/>  (' + mode +')<hr>'
//    html += 'nr local nodes: ' + d.nr_local_nodes +'<br>local position: ' + d.lpos + '<br>distance: ' +  d.neighbor_distance + '<hr>'

    table = false;

    if (d.rt.locals.predecessor || d.rt.locals.successor){
        html += '<br><span>Local</span> <br/>'
        html += '<table border="1" style="width:100%"><tr><th>role</th><th>id</th><th>latency</th></tr>'
        table = true;
    }


    if (d.rt.locals.predecessor){
        latency = d.rt.locals.predecessor.latency
        if(mode == 'editing'){
            latency = '<input id="llp" style="width:50px; text-align:center" value="'+latency+'"><br>'
         }
        html += '<tr><td>p</td><td>'+d.rt.locals.predecessor.id+'</td><td>'+latency +'</td></tr>'
    }

    if (d.rt.locals.successor){
        latency = d.rt.locals.successor.latency
        if(mode == 'editing')
            latency = '<input id="lls" style="width:50px; text-align:center" value="'+latency+'">'
        html += '<tr><td>s</td><td>'+d.rt.locals.successor.id+'</td><td>'+latency+'</td></tr>'
    }

    if (table){
        html += '</table>'
        table = false
    }


    if (d.rt.globals.predecessor || d.rt.globals.successor){
        html += '<br><span>Global</span> <br/>'
        html += '<table border="1" style="width:100%"><tr><th>role</th><th>id</th><th>latency</th></tr>'
        table = true;
    }


    if (d.rt.globals.predecessor){
        col = colors[d.rt.globals.predecessor.lq]
        checked = ['o','o', 'o', 'o']
        checked[d.rt.globals.predecessor.lq] = '+'
        latency = d.rt.globals.predecessor.latency
        if(mode == 'editing'){
            latency = '<input id="lgp" style="width:50px; text-align:center" type="number" value="'+latency+'">'
            latency += '<table id="lqgp" border="0" style="margin-top:2px" ><tr>'
            for(i=0;i<colors.length;i++)
                latency += '<td style="color:'+ colors[i] +'; padding:0px; margin:0px; font-weight: 900;" onclick="changeLQ(event)">' + checked[i] + '</td>'
            latency += '</tr></table>'
        }
        html += '<tr><td>p</td><td>'+d.rt.globals.predecessor.id+'</td><td style="color:'+col+'">'+latency+'</td></tr>'
    }
    if (d.rt.globals.successor){
        col = colors[d.rt.globals.successor.lq]
        checked = ['o','o', 'o', 'o']
        checked[d.rt.globals.successor.lq] = '+'
        latency = d.rt.globals.successor.latency
        if(mode == 'editing'){
            latency = '<input id="lgs" style="width:50px; text-align:center" type="number" value="'+latency+'">'
            latency += '<table id="lqgs" border="0" style="margin-top:2px" ><tr>'
            for(i=0;i<colors.length;i++)
                latency += '<td style="color:'+ colors[i] +'; padding:0px; margin:0px; font-weight: 900; cursor:pointer" onclick="changeLQ(event)">' + checked[i] + '</td>'
            latency += '</tr></table>'
        }
        html += '<tr><td>s</td><td>'+d.rt.globals.successor.id+'</td><td style="color:'+col+'">'+latency+'</td></tr>'
    }
    if (table)
        html += '</table>'


    html += '<div id="ll_ph" style="display:none"><br><span>Long links</span><br><table id="nll_tb" border="1" style="width:100%;"><tr><th>id</th><th>latency</th>'
    if(mode == 'editing')
        html += '<th></th>'

    if (d.rt.globals.longlinks.length > 0 ){
        for(i = 0; i < d.rt.globals.longlinks.length; i++){
            col = colors[d.rt.globals.longlinks[i].lq]
            checked = ['o','o', 'o', 'o']
            checked[d.rt.globals.longlinks[i].lq] = '+'
            latency = d.rt.globals.longlinks[i].latency
            if(mode == 'editing'){
                latency = '<input id="lgll'+i+'" style="width:50px; text-align:center" type="number" value="'+latency+'">'
                latency += '<table id="lqgll'+i+'" border="0" style="margin-top:2px" ><tr>'
                for(j=0;j<colors.length;j++)
                    latency += '<td style="color:'+ colors[j] +'; padding:0px; margin:0px; font-weight: 900; cursor:pointer" onclick="changeLQ(event)">' + checked[j] + '</td>'
                latency += '</tr></table>'
            }

            if(mode == 'editing'){
                html += '<tr><td><select>'+ get_ops(d, d.rt.globals.longlinks[i].id) +'<select></td><td style="color:'+col+'">'+latency+'</td>'
                html += '<td style="font-weight:900; cursor:pointer" onclick="delete_ll(event)">x</td>'
            }else
                html += '<tr><td>'+ d.rt.globals.longlinks[i].id +'</td><td style="color:'+col+'">'+latency+'</td>'
            html += '</tr>'
        }


    }

    html += '</table></div>'

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

        for(i = 0; i < d.rt.globals.visited.length; i++){
            col = colors[d.rt.globals.visited[i].lq]
            html += '<tr><td>'+d.rt.globals.visited[i].id+'</td><td style="color:'+col+'">'+d.rt.globals.visited[i].latency+'</td></tr>'
        }
        html += '</table>'
    }




    html += '</div>'

    if(mode == 'editing'){

        opts = get_ops(d, '')

        if (opts.length > 0){
            html += '<div id="new_ll_ph" style="display:none"><br><span>New long links</span><br><table id="nll_tb"></table></div>'
            html += '<br><input id="add_ll" type="button" value="Add long links" onclick="add_new_ll(opts)" />'
        }
        html += '<br><br><input id="done" type="button" value="Done"  />' //onclick="done_editing"



    }

    d3.select("#node_info").html(html);
    if (d.rt.globals.longlinks.length > 0 )
        $("#ll_ph").show()
    if(mode == 'editing')
        document.getElementById("done").addEventListener("click", function() {done_editing(d);}, false);
}

function get_ops(d, select){
    opts=''
    for(i=0;i<nodes.length;i++){
        if( nodes[i].id.split("-")[0] == d.id.split("-")[0]
            || (d.rt.globals.predecessor &&  nodes[i].id == d.rt.globals.predecessor.id)
            || (d.rt.globals.successor &&  nodes[i].id == d.rt.globals.successor.id))
            continue
        if (select.length > 0 && nodes[i].id == select)
            opts += '<option value="'+nodes[i].id+'" selected>' + nodes[i].id + '</option>'
        else
            opts += '<option value="'+nodes[i].id+'">' + nodes[i].id + '</option>'
    }
    return opts
}

function add_new_ll(opts){
    colors = ['red','orange', 'blue', 'green']
    html = '<tr><td><select>' + opts + '</select></td><td ><input id="lgll'+rand_str(3)+'" type="number" style="width:50px;">'
    checked = ['o','o', 'o', '+']
    html += '<table  style="margin-top:2px;" ><tr>'
        for(i=0;i<colors.length;i++)
            html += '<td style="color:'+ colors[i] +'; padding:0px; margin:0px; font-weight: 900; cursor:pointer" onclick="changeLQ(event)">' + checked[i] + '</td>'
        html += '</tr></table></td><td style="font-weight:900; cursor:pointer" onclick="delete_ll(event)">x</td></tr>'
    $("#nll_tb").append(html)
    $("#ll_ph").show()
}

function changeLQ(e){
//    table_id = $($(e.target).parents('table')[0]).attr('id')
    $($(e.target).parents('table')[0]).find('td').each(function () {
        $(this).html('o')
    });
    $(e.target).html('+')
}

function delete_ll(e){
    table = $($(e.target).parents('table')[0])
    $($(e.target).parents('tr')[0]).remove()
    if (table.find('td').length == 0)
        $("#ll_ph").hide()
}

function done_editing(d){
    lls = []
    $('#node_info :input[type="number"]').each(
        function(index){
            var input = $(this);
            id = input.attr('id')
            if(id == 'llp')
                d.rt.locals.predecessor.latency = input.val()
            if(id == 'lls')
                d.rt.locals.successor.latency = input.val()
            if(id == 'lgp'){
                d.rt.globals.predecessor.latency = input.val()
                tds = $('#lqgp').find('td')
                for(i=0;i<tds.length;i++)
                    if ($(tds[i]).html() == '+')
                        d.rt.globals.predecessor.lq = i

            }
            if(id == 'lgs'){
                d.rt.globals.successor.latency = input.val()
                tds = $('#lqgs').find('td')
                for(i=0;i<tds.length;i++)
                    if ($(tds[i]).html() == '+')
                        d.rt.globals.successor.lq = i

            }
            if(id.indexOf('lgll') > -1){
                e = {id:$(input.parents('tr')[0]).find('select').val(), latency:input.val(), lq:1}
                tds = $(input.parent()).find('table').find('td')
                for(i=0;i<tds.length;i++)
                    if ($(tds[i]).html() == '+')
                        e.lq = i
                if(e.latency.length > 0)
                    lls.push(e)
            }
        }
    );

    d.rt.globals.longlinks = lls

    // update node in nodes
    for(i=0;i<nodes.length;i++){
        if(nodes[i].id == d.id)
            nodes[i] = d
    }

    gmode = 'readonly'
    show_node_info(d, 'readonly')
}


function mouseover(d) {
    if (gmode == 'readonly'){
        show_node_info(d, 'readonly') //readonly
        d3.select(this.firstChild).attr("r", 10);
    }


}

function dblclick(d){
    md = 'editing'
    gmode = md
    show_node_info(d, md)
}

function mouseout() {
  if (gmode!='editing'){
    d3.select("#node_info").html('');

    if (d3.select(this).attr('class') == 'link')
        d3.select(this).attr("stroke-width", "1.5")
    else
        d3.select(this.firstChild).attr("r", 5);
  }

}

function linkmouseover(l)
{
    if (gmode == 'readonly'){
        srcs = []
        trgts = []
        relations = []
        latencies = []

        is_local = (l.source.id.split('-')[0] == l.target.id.split('-')[0])

        neigs_s = is_local ? l.source.rt.locals : l.source.rt.globals
        neigs_t = is_local ? l.target.rt.locals : l.target.rt.globals


        if(neigs_s.predecessor.id == l.target.id || neigs_s.successor.id == l.target.id ){
            srcs.push(l.source.id)
            trgts.push(l.target.id)
            if (neigs_s.predecessor.id == l.target.id){
                relations.push('predecessor')
                latencies.push(neigs_s.predecessor)
            }else{
                relations.push('successor')
                latencies.push(neigs_s.successor)
             }
        }

        if(neigs_t.predecessor.id == l.source.id || neigs_t.successor.id == l.source.id ){
            srcs.push(l.target.id)
            trgts.push(l.source.id)
            if (neigs_t.predecessor.id == l.source.id){
                relations.push('predecessor')
                latencies.push(neigs_t.predecessor)
            }else{
                relations.push('successor')
                latencies.push(neigs_t.successor)
            }
        }
        html = '<br>'

        final_srcs = srcs
        final_trgts = trgts
        final_rs = relations
        final_l = latencies

        if(srcs.length > 1 && relations[1]=='successor'){
            final_srcs = [srcs[1], srcs[0]]
            final_trgts = [trgts[1], trgts[0]]
            final_rs = [relations[1], relations[0]]
            final_l = [latencies[1], latencies[0]]
        }

        colors = ['red','orange', 'blue', 'green']
        for(i=0;i<final_srcs.length;i++)
        {
            html += '<strong>' + final_srcs[i] + '</strong> has <strong>' + final_trgts[i] + '</strong> as <br> <strong> ' + final_rs[i] +'</strong><br>'
            html += 'Latency: <span style="color:'+colors[final_l[i].lq] +'">' + final_l[i].latency + '</span>'
            html += '<br><br>'
        }
        d3.select(this).attr("stroke-width", "5")
        d3.select("#node_info").html(html);
    }


}



function show_mapping(){
    l = 'abcdefghijklmnopqrstuvwxyz'
    mp = ''
    for(i=0; i < l.length; i++){
        mp += (i+1) + '->' +  l.charAt(i) + '<br>'
    }

    txt = '<div style="text-align:center; padding:5px" >'+mp+'</div>'
    if (gmode == 'readonly')
        $("#node_info").html(txt);
}

function hide_mapping(){
    if (gmode == 'readonly')
        $("#node_info").html('');
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
            new_nodes.push({"id":snodes[i].id, "group":snodes[i].dc_id, 'rt':snodes[i].rt /*, 'nr_local_nodes': snodes[i].nr_local_nodes, 'lpos':snodes[i].lpos, 'neighbor_distance': snodes[i].neighbor_distance */});

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
        h = $(el).parent().innerHeight();

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
            .attr("class", "link")
            .attr("stroke-width", "1.5")
            .on("mouseover", linkmouseover)
            .on("mouseout", mouseout);

        link.exit().remove();

        var node = vis.selectAll("g.node")
            .data(nodes, function(d) { return d.id;});

        var nodeEnter = node.enter().append("g")
            .attr("class", "node")
            .on("mouseover", mouseover)
            .on("mouseout", mouseout)
            .on("dblclick", dblclick)
            .call(force.drag);

        node.append("circle")
        .style("fill", function(d) { return color(d.group);})
//        .attr("r", 4);
        .attr("r", 5);

        nodeEnter.append("text")
            .attr("class", "nodetext")
            .attr("dx", 12)
            .attr("dy", ".35em")
//            .attr("fill", "#B0171F")
            .text(function(d) {return d.id});

        node.exit().remove();

        force.linkDistance(function(d) { if (d.source.group == d.target.group) return 10; else return 100;  });
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


function rand_str(length)
{
    var text = "";
    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    for( var i=0; i < length; i++ )
        text += possible.charAt(Math.floor(Math.random() * possible.length));

    return text;
}
