package cz.muni.fi.jboss.migration;


import org.jboss.as.cli.batch.Batch;
import org.jboss.as.cli.batch.impl.DefaultBatch;
import org.jboss.as.cli.batch.impl.DefaultBatchedCommand;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;

import javax.security.auth.callback.*;
import javax.security.sasl.RealmCallback;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * @author Roman Jakubco
 *         Date: 4/4/13
 *         Time: 8:19 PM
 */
public class Test {
    public static void main(String[] args) {


        ModelNode request = new ModelNode();
        request.get(ClientConstants.OP).set(ClientConstants.ADD);
        request.get(ClientConstants.OP_ADDR).add("subsystem", "datasources");
        request.get(ClientConstants.OP_ADDR).add("data-source", "test1");
        request.get("jndi-name").set("java:/datasource1");
        request.get("pool-name").set("test1");
        request.get("enabled").set(true);
        request.get("connection-url").set("jdbc:h2:mem:test;");
        request.get("driver-name").set("h2");

        ModelNode request2 = new ModelNode();
        request2.get(ClientConstants.OP).set(ClientConstants.ADD);
        request2.get(ClientConstants.OP_ADDR).add("subsystem", "datasources");
        request2.get(ClientConstants.OP_ADDR).add("data-source", "test2");
        request2.get("jndi-name").set("java:/datasource2");
        request2.get("pool-name").set("test2");
        request2.get("enabled").set("true");
        request2.get("connection-url").set("jdbc:h2:mem:test;");
        request2.get("driver-name").set("h2");

        ModelNode request3 = new ModelNode();
        request3.get(ClientConstants.OP).set(ClientConstants.ADD);
        request3.get(ClientConstants.OP_ADDR).add("subsystem", "datasources");
        request3.get(ClientConstants.OP_ADDR).add("data-source", "test3");
        request3.get("jndi-name").set("java:/datasource3");
        request3.get("pool-name").set("test3");
        request3.get("enabled").set("true");
        request3.get("connection-url").set("jdbc:h2:mem:test;");
        request3.get("driver-name").set("h2");


        Batch test = new DefaultBatch();

        DefaultBatchedCommand g = new DefaultBatchedCommand("test", request);
        DefaultBatchedCommand g2 = new DefaultBatchedCommand("test2", request2);
        DefaultBatchedCommand g3 = new DefaultBatchedCommand("test3", request3);

        test.add(g);
        test.add(g2);
        test.add(g3);
//        try {
//            getDataSources();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        ModelControllerClient client = null;

// Try connecting to the server without authentication first

        ModelControllerClient unauthenticatedClient = null;
        try {

            unauthenticatedClient = ModelControllerClient.Factory.create(InetAddress.getByName("localhost"), 9999);


            ModelNode testConnection = new ModelNode();
            testConnection.get("operation").set("read-resource");  // Execute an operation to see if we need to authenticate
            unauthenticatedClient.execute(testConnection);
            client = unauthenticatedClient;
        } catch (Exception e) {
            try {
                ModelNode handlerCmd = new ModelNode();
                handlerCmd.get(ClientConstants.OP).set(ClientConstants.ADD);
                handlerCmd.get(ClientConstants.OP_ADDR).add("subsystem", "logging");
                handlerCmd.get(ClientConstants.OP_ADDR).add("root-logger", "ROOT");


                ModelNode op = new ModelNode();
                op.get("operation").set("write-attribute");
                ModelNode addr = op.get("address");

                addr.add("subsystem", "logging");
                addr.add("root-logger", "ROOT");

//                op.get("name").set("level");
//                op.get("value").set("INFO");
                op.get("name").set("filter");
                op.get("value").set("all");

                System.out.println(op);


                client = ModelControllerClient.Factory.create(InetAddress.getByName("localhost"), 9999);
                ModelNode returnVal = client.execute(new OperationBuilder(op).build());
                System.out.println(returnVal.toString());
                System.out.println(returnVal.get("result").toString());


                safeClose(client);
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                safeClose(client);
            }

        }

    }

    public static void executeRequest(ModelNode request) throws Exception {
        ModelControllerClient client = null;
        try {
            client = ModelControllerClient.Factory.create("localhost", 9999);
            final ModelNode response = client.execute(new OperationBuilder(request).build());
            reportFailure(response);
        } finally {
            safeClose(client);
        }
    }


    public static void safeClose(final Closeable closeable) {
        if (closeable != null) try {
            closeable.close();
        } catch (Exception e) {
            // no-op
        }
    }

    private static void reportFailure(final ModelNode node) {
        if (!node.get(ClientConstants.OUTCOME).asString().equals(ClientConstants.SUCCESS)) {
            final String msg;
            if (node.hasDefined(ClientConstants.FAILURE_DESCRIPTION)) {
                if (node.hasDefined(ClientConstants.OP)) {
                    msg = String.format("Operation '%s' at address '%s' failed: %s", node.get(ClientConstants.OP), node.get(ClientConstants.OP_ADDR), node.get(ClientConstants.FAILURE_DESCRIPTION));
                } else {
                    msg = String.format("Operation failed: %s", node.get(ClientConstants.FAILURE_DESCRIPTION));
                }
            } else {
                msg = String.format("Operation failed: %s", node);
            }
            throw new RuntimeException(msg);
        }
    }


    public static List<ModelNode> getDataSources() throws IOException {
        final ModelNode request = new ModelNode();
        request.get(ClientConstants.OP).set("read-resource");
        request.get("recursive").set(true);
        request.get(ClientConstants.OP_ADDR).add("subsystem", "datasources");
        ModelControllerClient client = null;
        try {
            client = ModelControllerClient.Factory.create(InetAddress.getByName("localhost"), 9999);

            final ModelNode response = client.execute(request);

            return response.get(ClientConstants.RESULT).get("data-source").asList();
        } finally {
            safeClose(client);
        }
    }

    static ModelControllerClient createClient(final InetAddress host, final int port,
                                              final String username, final char[] password, final String securityRealmName) {

        final CallbackHandler callbackHandler = new CallbackHandler() {

            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (Callback current : callbacks) {
                    if (current instanceof NameCallback) {
                        NameCallback ncb = (NameCallback) current;
                        ncb.setName(username);
                    } else if (current instanceof PasswordCallback) {
                        PasswordCallback pcb = (PasswordCallback) current;
                        pcb.setPassword(password);
                    } else if (current instanceof RealmCallback) {
                        RealmCallback rcb = (RealmCallback) current;
                        rcb.setText(rcb.getDefaultText());
                    } else {
                        throw new UnsupportedCallbackException(current);
                    }
                }
            }
        };

        return ModelControllerClient.Factory.create(host, port, callbackHandler);
    }


}

