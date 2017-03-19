<html>
	<head>
		<title>Minu-tervis</title>
	</head>
	<body>
		<p>Tere tulemast!</p>
		<?php
	    $json = file_get_contents("php://input");
	    $obj = json_decode($json);
	    print_r($obj["body"]["measuregrps"]);
		?>
	</body>
</html>