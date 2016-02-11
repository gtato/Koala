from flask import Flask, render_template, request, jsonify, send_from_directory
from koala.protocol import Koala
from koala.node import Node
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


@app.route('/add_node')
def add_node():
    node_id = request.args.get('node_id', '', type=str)
    boot_node_id = request.args.get('boot_node_id', '', type=str)

    nsplit = node_id.split('-')
    node = Node(nsplit[1], nsplit[0])
    node.join(boot_node_id)


    return jsonify(result=True)


@app.route('/get_nodes')
def get_nodes():
    nodes = []
    for n in Koala.nodes:
        nodes.append(n.to_dict())
        # print '%s rt is: %s ' % ( n.id, n.rt.to_dict())
    
    return jsonify(result=nodes)


@app.route('/delete_nodes')
def delete_nodes():
    Koala.nodes = []
    return jsonify(result=True)



if __name__ == '__main__':
    n1 = Node('5', 'a')
    n1.join('')

    n2 = Node('10', 'a')
    n2.join('a-5')

    n3 = Node('20', 'a')
    n3.join('a-10')

    app.run(
        host="0.0.0.0",
        port=int("8080"),
        debug=True
    )