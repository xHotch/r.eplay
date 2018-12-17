package at.ac.tuwien.sepm.assignment.group.replay;

import at.ac.tuwien.sepm.assignment.group.util.JDBCConnectionManager;

class MockDatabase {

    private static JDBCConnectionManager jdbcConnectionManager = new JDBCConnectionManager();
    private static String connectionString = "jdbc:h2:~/qse01-replay-test;INIT=RUNSCRIPT FROM 'classpath:sql/createAndInsertTest.sql';USER=dbUser;PASSWORD=dbPassword";

    static JDBCConnectionManager getJDBCConnectionManager(){
        jdbcConnectionManager = new JDBCConnectionManager();
        jdbcConnectionManager.setConnectionString(connectionString);
        return jdbcConnectionManager;
    }

}
