/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. All rights reserved.
 * Extension (c) 2012 Scott Yeskie

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; version 2 of
   the License.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA */

#include <stdio.h>
#include <string.h>
#include <mysql/plugin.h>
#include <stdlib.h>
#include "plugin_audit.h"

#if !defined(__attribute__) && (defined(__cplusplus) || !defined(__GNUC__)  || __GNUC__ == 2 && __GNUC_MINOR__ < 8)
#define __attribute__(A)
#endif

static volatile int number_of_calls; /* for SHOW STATUS, see below */

/*
  Initialize the plugin at server start or plugin installation.

  SYNOPSIS
    audit_null_plugin_init()

  DESCRIPTION
    Does nothing.

  RETURN VALUE
    0                    success
    1                    failure (cannot happen)
*/

static int audit_logger_plugin_init(void *arg __attribute__((unused)))
{
  number_of_calls= 0;
  system("touch /tmp/rtlog.sql");
  system("chmod 777 /tmp/rtlog.sql");
  return(0);
}


/*
  Terminate the plugin at server shutdown or plugin deinstallation.

  SYNOPSIS
    audit_null_plugin_deinit()
    Does nothing.

  RETURN VALUE
    0                    success
    1                    failure (cannot happen)

*/

static int audit_logger_plugin_deinit(void *arg __attribute__((unused)))
{
  return(0);
}


/*
  Foo

  SYNOPSIS
    audit_null_notify()
      thd                connection context

  DESCRIPTION
*/

static void audit_logger_notify(MYSQL_THD thd __attribute__((unused)),
                              unsigned int event_class,
                              const void *event)
{
  /* prone to races, oh well */
  number_of_calls++;
  if (event_class == MYSQL_AUDIT_GENERAL_CLASS)
  {
    const struct mysql_event_general *event_general=
      (const struct mysql_event_general *) event;
    if(event_general->event_subclass == MYSQL_AUDIT_GENERAL_RESULT) {
      char log_user[] = "logger";
      //strcmp returns 0 on equal which maps to FALSE
      if(strncmp(event_general->general_user, log_user, 6) &&
	event_general->general_query_length > 0) {
        FILE* fp = fopen("/tmp/rtlog.sql","a");
        //fputc('\n', fp);
	//fwrite(event_general->general_user, 1, event_general->general_user_length, fp);
        fputc('{', fp);
        fputc('\n', fp);
        fwrite(event_general->general_query, 1, event_general->general_query_length, fp);
        fputc('\n', fp);
        fputc('}', fp);
        fputc('\n', fp);
        fclose(fp);
      }
    }
  }
}


/*
  Plugin type-specific descriptor
*/

static struct st_mysql_audit audit_logger_descriptor=
{
  MYSQL_AUDIT_INTERFACE_VERSION,                    /* interface version    */
  NULL,                                             /* release_thd function */
  audit_logger_notify,                                /* notify function      */
  { (unsigned long) MYSQL_AUDIT_GENERAL_CLASSMASK } /* class mask           */
};

/*
  Plugin status variables for SHOW STATUS
*/

static struct st_mysql_show_var simple_status[]=
{
  { "Audit_logger_called", (char *) &number_of_calls, SHOW_INT },
  //{ "Audit_logger_inlog", (char *) &logger_in_path, SHOW_STRING },
  { 0, 0, 0}
};


/*
  Plugin library descriptor
*/

mysql_declare_plugin(audit_logger)
{
  MYSQL_AUDIT_PLUGIN,         /* type                            */
  &audit_logger_descriptor,     /* descriptor                      */
  "AUDIT_LOGGER",               /* name                            */
  "Scott Yeskie",              /* author                          */
  "Realtime logger as audit plugin",        /* description                     */
  PLUGIN_LICENSE_GPL,
  audit_logger_plugin_init,     /* init function (when loaded)     */
  audit_logger_plugin_deinit,   /* deinit function (when unloaded) */
  0x0001,                     /* version                         */
  simple_status,              /* status variables                */
  NULL,                       /* system variables                */
  NULL,
  0,
}
mysql_declare_plugin_end;

int main( void ) {
  return 0;
}
