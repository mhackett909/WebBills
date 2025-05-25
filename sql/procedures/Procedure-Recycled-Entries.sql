CREATE PROCEDURE delete_old_entries_and_payments()
BEGIN
  DELETE FROM bills.payment
  WHERE entryID IN (
    SELECT id FROM bills.entry
    WHERE recycle_date IS NOT NULL
      AND recycle_date <= NOW() - INTERVAL 14 DAY
  );

  DELETE FROM bills.entry
  WHERE recycle_date IS NOT NULL
    AND recycle_date <= NOW() - INTERVAL 14 DAY;
END;