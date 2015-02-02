AES的加密/解密使用key和iv来运行算法，使用CBC模式
key是固定的256bits(32bytes)的随机化字符串
iv是每次请求都会改变的128bits(16bytes)的随机化字符串
iv会每次跟随请求发送到客户端

接口返回的数据类似于

```
DhMR7xJZMqWaC151fYBMPw==:/sQP6T9QGmott+eb10NSOCkvqAEYufu/FEICt/WP59C3lIsGnW+2XgZ12CcLV0im0czih6EISjp2UsB0xIxcf9tpCWSUANLQLtMSTkOiQQtdkauDd5UYB1I0Pwv5VBAeraOF3OlTDWFJin97TRcc3prlCIvE+5X3qnNk7lIxuUS6FEnEVDFawNLL1blISBg8SjOdxlWhshry+zec0Qg8EIA6GtnyQ1dEjltx0TaSzQqrkHQKaJxqwgNj6Yd+5OxWWcNjnZOFriIbyTjjj3+szCtwYa70Gwiizd6fWiPXCc7Nw8tciMc9zpHu0biKInEn86pY/HwqNd7dSocW1NfmZeHu/uvN8VOCv+NMzdlTobJf8Fg0EMlViikYIg9ymc5qesymXdiQmAekjxhZ0U5PDub6Gs7OUlE76iqsvaXA9hqx+qdsNWlXuOSRwmmoper7SDP+JN29jiluSjZEnaHWRog+fBvkcziad5c+dKtXti8=
```

冒号前端是iv，冒号后端是加密后的密文
iv和密文都使用了base64进行编码转换

解密的过程
1. 使用分号将结果分解成两部分，前端是iv，后端是密文

```
iv = 'DhMR7xJZMqWaC151fYBMPw=='
etext = '/sQP6T9QGmott+eb10NSOCkvqAEYufu/FEICt/WP59C3lIsGnW+2XgZ12CcLV0im0czih6EISjp2UsB0xIxcf9tpCWSUANLQLtMSTkOiQQtdkauDd5UYB1I0Pwv5VBAeraOF3OlTDWFJin97TRcc3prlCIvE+5X3qnNk7lIxuUS6FEnEVDFawNLL1blISBg8SjOdxlWhshry+zec0Qg8EIA6GtnyQ1dEjltx0TaSzQqrkHQKaJxqwgNj6Yd+5OxWWcNjnZOFriIbyTjjj3+szCtwYa70Gwiizd6fWiPXCc7Nw8tciMc9zpHu0biKInEn86pY/HwqNd7dSocW1NfmZeHu/uvN8VOCv+NMzdlTobJf8Fg0EMlViikYIg9ymc5qesymXdiQmAekjxhZ0U5PDub6Gs7OUlE76iqsvaXA9hqx+qdsNWlXuOSRwmmoper7SDP+JN29jiluSjZEnaHWRog+fBvkcziad5c+dKtXti8='
```

2. 使用base64的decode获取真实的iv字符串

```
iv = '\x0e\x13\x11\xef\x12Y2\xa5\x9a\x0b^u}\x80L?'
```

3. 使用base64的decode获取真实的密文字符串

```
etext = '\xfe\xc4\x0f\xe9?P\x1aj-\xb7\xe7\x9b\xd7CR8)/\xa8\x01\x18\xb9\xfb\xbf\x14B\x02\xb7\xf5\x8f\xe7\xd0\xb7\x94\x8b\x06\x9do\xb6^\x06u\xd8\'\x0bWH\xa6\xd1\xcc\xe2\x87\xa1\x08J:vR\xc0t\xc4\x8c\\\x7f\xdbi\td\x94\x00\xd2\xd0.\xd3\x12NC\xa2A\x0b]\x91\xab\x83w\x95\x18\x07R4?\x0b\xf9T\x10\x1e\xad\xa3\x85\xdc\xe9S\raI\x8a\x7f{M\x17\x1c\xde\x9a\xe5\x08\x8b\xc4\xfb\x95\xf7\xaasd\xeeR1\xb9D\xba\x14I\xc4T1Z\xc0\xd2\xcb\xd5\xb9HH\x18<J3\x9d\xc6U\xa1\xb2\x1a\xf2\xfb7\x9c\xd1\x08<\x10\x80:\x1a\xd9\xf2CWD\x8e[q\xd16\x92\xcd\n\xab\x90t\nh\x9cj\xc2\x03c\xe9\x87~\xe4\xecVY\xc3c\x9d\x93\x85\xae"\x1b\xc98\xe3\x8f\x7f\xac\xcc+pa\xae\xf4\x1b\x08\xa2\xcd\xde\x9fZ#\xd7\t\xce\xcd\xc3\xcb\\\x88\xc7=\xce\x91\xee\xd1\xb8\x8a"q\'\xf3\xaaX\xfc|*5\xde\xddJ\x87\x16\xd4\xd7\xe6e\xe1\xee\xfe\xeb\xcd\xf1S\x82\xbf\xe3L\xcd\xd9S\xa1\xb2_\xf0X4\x10\xc9U\x8a)\x18"\x0fr\x99\xcejz\xcc\xa6]\xd8\x90\x98\x07\xa4\x8f\x18Y\xd1NO\x0e\xe6\xfa\x1a\xce\xceRQ;\xea*\xac\xbd\xa5\xc0\xf6\x1a\xb1\xfa\xa7l5iW\xb8\xe4\x91\xc2i\xa8\xa5\xea\xfbH3\xfe$\xdd\xbd\x8e)nJ6D\x9d\xa1\xd6F\x88>|\x1b\xe4s8\x9aw\x97>t\xabW\xb6/'
```

4. 使用CBC模式的AES来进行解密，key和iv

```
encrypt(key, iv, etext)
```

解密后得到的原文

```
'{"msg": "get uplaod token successfully", "code": 1, "data": {"token": "1-ZM6fcKIPEBVshewXDTEbs8nC7-4UkhLUnj3eGW:fq54BnOiGrdW-Gjv9rDGbAp48vY=:eyJzY29wZSI6Inlpc2h1biIsImRlYWRsaW5lIjoxNDIwNTAwNjMzLCJjYWxsYmFja0JvZHkiOiJmaWxlbmFtZT0kKGZuYW1lKSZoYXNoPSQoZXRhZykiLCJjYWxsYmFja1VybCI6Imh0dHA6Ly9tb21lbnQuaHVzdG9ubGluZS5uZXQvYXBpL3YyL3VwbG9hZF9jYWxsYmFjayJ9"}}\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00'
```


5. 因为AES是基于块加密的算法，所以加密密文的长度必须是16的倍数，所以在加密之前对明文串末尾补偿`\0`解密之后应该使用rtrim之类的函数将其去掉

两篇stackoverflow关于oc如何进行二进制字符串和十六进制字符串转换
bin to hex
[http://stackoverflow.com/questions/8265525/converting-binary-bits-to-hex-value](http://stackoverflow.com/questions/8265525/converting-binary-bits-to-hex-value)
hex to bin
[http://stackoverflow.com/questions/7194528/how-to-convert-hex-to-binary-iphone](http://stackoverflow.com/questions/7194528/how-to-convert-hex-to-binary-iphone)
[http://stackoverflow.com/questions/22394564/hexadecimal-to-binary-in-objective-c](http://stackoverflow.com/questions/22394564/hexadecimal-to-binary-in-objective-c)