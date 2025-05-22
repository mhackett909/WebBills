import bcrypt

def generate_bcrypt_hash(plain_text_password: str, cost: int = 10) -> str:
    salt = bcrypt.gensalt(rounds=cost)
    # Hash the password
    hashed = bcrypt.hashpw(plain_text_password.encode('utf-8'), salt)
    return hashed.decode('utf-8')

if __name__ == "__main__":
    password = input("Enter a password to hash: ")
    cost = input("Enter cost (default is 10): ").strip()

    if cost.isdigit():
        cost = int(cost)
    else:
        cost = 10  # default

    hashed_password = generate_bcrypt_hash(password, cost)
    print("\nHashed password (store this in your DB):\n")
    print(hashed_password)
