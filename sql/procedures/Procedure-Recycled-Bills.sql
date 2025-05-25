CREATE PROCEDURE delete_old_bills_and_deps()
BEGIN
  -- Delete payments for entries under bills to be deleted
  DELETE FROM bills.payment
  WHERE entryID IN (
    SELECT e.id
    FROM bills.entry e
    JOIN bills.bill b ON e.billID = b.id
    WHERE b.recycle_date IS NOT NULL
      AND b.recycle_date <= NOW() - INTERVAL 14 DAY
  );

  -- Delete entries under those bills
  DELETE FROM bills.entry
  WHERE billID IN (
    SELECT id
    FROM bills.bill
    WHERE recycle_date IS NOT NULL
      AND recycle_date <= NOW() - INTERVAL 14 DAY
  );
  
  -- Delete the bills
  DELETE FROM bills.bill
  WHERE recycle_date IS NOT NULL
    AND recycle_date <= NOW() - INTERVAL 14 DAY;
END;
