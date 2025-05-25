CREATE PROCEDURE delete_old_payments()
BEGIN
  DELETE FROM bills.payment
  WHERE recycle_date IS NOT NULL
    AND recycle_date <= NOW() - INTERVAL 14 DAY;
END;