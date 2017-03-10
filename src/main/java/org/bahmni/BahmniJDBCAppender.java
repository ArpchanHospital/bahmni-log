package org.bahmni;

import org.apache.log4j.jdbc.JDBCAppender;
import org.apache.log4j.spi.LoggingEvent;

import java.sql.SQLException;
import java.util.Iterator;

/**
 * BahmniJDBCAppender is just a sample class to create log and write it in DB.
 * Sample Log file format and the log message should be separated some special character,
 * we have considered "^^^" as separator.
 * User_id ^^^ Patient_id^^^Action_type^^^Message
 * Eg: log.error(Context.getAuthenticatedUser().getId()+"^^^74^^^VIEW^^^Sample debug message");
 */
public class BahmniJDBCAppender extends JDBCAppender {

    private final String SQL = "INSERT INTO bahmni_logs(user_id, patient_id, level, logger_class, event, message, date_created, uuid) " +
            "VALUES('%s', '%s', '%s', '%s', '%s', '%s', now(), uuid())";
    @Override
    public void flushBuffer() {
        this.removes.ensureCapacity(this.buffer.size());
        Iterator i = this.buffer.iterator();

        while(i.hasNext()) {
            try {
                LoggingEvent e = (LoggingEvent)i.next();
                if(e.getMessage().toString().contains("^^^")) {
                    String[] messageLog = e.getMessage().toString().split("\\^\\^\\^");
                    String sqlQuery = String.format(SQL, messageLog[0], messageLog[1], e.getLevel().toString(), e.categoryName, messageLog[2], messageLog[3]);
                    this.execute(sqlQuery);
                }
                this.removes.add(e);
            } catch (SQLException var4) {
                super.errorHandler.error("Failed to excute sql", var4, 2);
            }
        }

        this.buffer.removeAll(this.removes);
        this.removes.clear();
    }
}
