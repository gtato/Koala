from flask import Flask, render_template, request, jsonify, send_from_directory
import simplejson as json
from koala.protocol import Koala
from koala.node import Node
from koala.message import Message
import os
# Initialize the Flask application
app = Flask(__name__)


# This route will show a form to perform an AJAX request
# jQuery is loaded to execute the request and update the
# value of the operation
@app.route('/')
def index():
    return render_template('index.html')


@app.route('/js/<path:path>')
def send_js(path):
    return send_from_directory('js', path)


@app.route('/css/<path:path>')
def send_css(path):
    return send_from_directory('css', path)

@app.route('/images/<path:path>')
def send_image(path):
    return send_from_directory('images', path)


@app.route('/add_node')
def add_node():
    Koala.reset_msgs()
    node_id = request.args.get('node_id', '', type=str)
    boot_node_id = request.args.get('boot_node_id', '', type=str)

    nsplit = node_id.split('-')
    node = Node(nsplit[1], nsplit[0])
    node.join(boot_node_id)

    # print 'after adding %s, consistent: %s' % (node_id,Koala.detect_inconsitent_position())

    return jsonify(result=True, msgs=Koala.get_nr_msgs())


@app.route('/add_list')
def add_list():
    Koala.reset_msgs()
    Koala.nodes = []
    lnodes = request.args.get('nodes', '', type=str)
    nodes = lnodes.split(",")
    for ln in nodes:
        ln_split = ln.split('|')
        nsplit = ln_split[0].split('-')
        node = Node(nsplit[1].strip(), nsplit[0].strip())
        node.join(ln_split[1].strip())

    nodes = []
    for n in Koala.nodes:
        nodes.append(n.to_dict())

    return jsonify(result=nodes, msgs=Koala.get_nr_msgs())


@app.route('/route_list')
def route_list():
    Koala.reset_msgs()

    lroutes = request.args.get('routes', '', type=str)
    routes = lroutes.split(",")
    paths = []
    for lr in routes:
        lr_split = lr.split('->')
        start = lr_split[0].strip()
        dest = lr_split[1].strip()

        try:
            path = Koala.send_to(start, start, Message('route', dest))
        except:
            print "routing %s -> %s" % (start, dest)
        paths.append(path)


    return jsonify(result=paths, msgs=Koala.get_nr_msgs())




@app.route('/get_nodes')
def get_nodes():
    nodes = []
    for n in Koala.nodes:
        nodes.append(n.to_dict())
        # print '%s rt is: %s ' % ( n.id, n.rt.to_dict())
    
    return jsonify(result=nodes)


@app.route('/route')
def route():
    Koala.reset_msgs()
    start = request.args.get('from', '', type=str)
    dest = request.args.get('to', '', type=str)

    path = Koala.send_to(start, start, Message('route', dest))


    return jsonify(result=path, msgs=Koala.get_nr_msgs())


@app.route('/delete_nodes')
def delete_nodes():
    Koala.nodes = []
    return jsonify(result=True)



@app.route('/restore', methods=['POST'])
def upload():
    # Get the name of the uploaded file
    file = request.files['file']
    json_nodes = json.loads(file.read())
    nodes = [Node.from_dict(jn) for jn in json_nodes]
    Koala.nodes = nodes
    return jsonify(result=json_nodes)

if __name__ == '__main__':
    # n1 = Node('5', 'a')
    # n1.join('')
    #
    # n2 = Node('10', 'a')
    # n2.join('a-5')
    #
    # n3 = Node('20', 'a')
    # n3.join('a-10')
    prt = "8080"
    loc = '%s' % os.getcwd()
    if 'tatoal' in loc:
        prt = "56789"

    app.run(
        host="0.0.0.0",
        port=int(prt),
        debug=True
    )