DROP TRIGGER IF EXISTS bills.trg_update_recycle_date_bill;

DELIMITER //

CREATE TRIGGER trg_update_recycle_date_bill
AFTER UPDATE ON bills.bill
FOR EACH ROW
BEGIN
    -- Only proceed if the recycle_date has actually changed (including null-safe check)
    IF (NEW.recycle_date IS NOT NULL AND OLD.recycle_date IS NULL) 
       OR (NEW.recycle_date IS NULL AND OLD.recycle_date IS NOT NULL)
       OR (NEW.recycle_date <> OLD.recycle_date) THEN

        -- Update recycle_date in bills.entry (allow NULL to cascade)
        UPDATE bills.entry 
        SET recycle_date = NEW.recycle_date 
        WHERE billID = NEW.id;

        -- Directly update recycle_date in bills.payment for the affected entries (allow NULL to cascade)
        UPDATE bills.payment 
        SET recycle_date = NEW.recycle_date 
        WHERE entryID IN (
            SELECT id 
            FROM bills.entry 
            WHERE billID = NEW.id
        );
    END IF;
END //

DELIMITER ;

DROP TRIGGER IF EXISTS bills.trg_update_recycle_date_entry;

DELIMITER //

CREATE TRIGGER trg_update_recycle_date_entry
AFTER UPDATE ON bills.entry
FOR EACH ROW
BEGIN
    -- Only proceed if the recycle_date has actually changed (including null-safe check)
    IF (NEW.recycle_date IS NOT NULL AND OLD.recycle_date IS NULL) 
       OR (NEW.recycle_date IS NULL AND OLD.recycle_date IS NOT NULL)
       OR (NEW.recycle_date <> OLD.recycle_date) THEN

        -- Directly update recycle_date in bills.payment (allow NULL to cascade)
        UPDATE bills.payment 
        SET recycle_date = NEW.recycle_date 
        WHERE entryID = NEW.id;
    END IF;
END //

DELIMITER ;

