cmake_minimum_required(VERSION 2.8)

MYSQL_ADD_PLUGIN(audit_logger audit_logger.c
  MODULE_ONLY MODULE_OUTPUT_NAME "adt_logger")