import hashlib
input_string = "testblock"
v_value = 1074954687
algorithm = "sha256"

def int_to_base26_string(v):
    characters = []
    while v > 0:
        characters.append(chr(97 + v % 26))
        v //= 26
    while len(characters) < 7:
        characters.append('a')
    return ''.join(reversed(characters))

v_string = int_to_base26_string(v_value)
data_to_hash = input_string + v_string
hash_result = hashlib.new(algorithm, data_to_hash.encode()).hexdigest()

print(hash_result.startswith('00000'), hash_result)
