CREATE PROCEDURE delete_old_users_and_deps()
BEGIN
  -- Delete payments under entries tied to bills owned by users to be deleted
  DELETE FROM bills.payment
  WHERE entryID IN (
    SELECT e.id
    FROM bills.entry e
    JOIN bills.bill b ON e.billID = b.id
    JOIN bills.users u ON b.userID = u.id
    WHERE u.recycle_date IS NOT NULL
      AND u.recycle_date <= NOW() - INTERVAL 30 DAY
  );

  -- Delete entries under bills owned by those users
  DELETE FROM bills.entry
  WHERE billID IN (
    SELECT b.id
    FROM bills.bill b
    JOIN bills.users u ON b.userID = u.id
    WHERE u.recycle_date IS NOT NULL
      AND u.recycle_date <= NOW() - INTERVAL 30 DAY
  );

  -- Delete bills owned by users to be deleted
  DELETE FROM bills.bill
  WHERE userID IN (
    SELECT id
    FROM bills.users
    WHERE recycle_date IS NOT NULL
      AND recycle_date <= NOW() - INTERVAL 30 DAY
  );

  -- Delete the users
  DELETE FROM bills.users
  WHERE recycle_date IS NOT NULL
    AND recycle_date <= NOW() - INTERVAL 30 DAY;
END;
