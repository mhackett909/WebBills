CREATE PROCEDURE cleanup_all_recycled_data()
BEGIN
  CALL delete_old_payments();
  CALL delete_old_entries_and_payments();
  CALL delete_old_bills_and_deps();
  CALL delete_old_users_and_deps();
END;