<html>
	<head>
		<title>Minu-tervis</title>
	</head>
	<body>
		<p>Tere tulemast!</p>

		<?php
		$url = parse_url(getenv("CLEARDB_DATABASE_URL"));

		$server = $url["host"];
		$username = $url["user"];
		$password = $url["pass"];
		$db = substr($url["path"], 1);

		$conn = new mysqli($server, $username, $password, $db);
		if ($conn->connect_error) {
		    die("Connection failed: " . $conn->connect_error);
		}

		$json = file_get_contents("php://input");
		$obj = json_decode($json, true);

	    $user_personal_id = $obj["user_personal_id"];

	    $measuregrps = $obj["body"]["measuregrps"];
	    $n = count($measuregrps);
	  	for ($i = 0; $i < $n; $i++) {
	  		$grp = $measuregrps[$i];

	  		$date = $grp["date"];

	  		$sys = 0;
	  		$dia = 0;
	  		$pulse = 0;

		    $measures = $grp["measures"];
		    $m = count($measures);
		    if ($m == 3) {
		    	for ($j = 0; $j < $m; $j++) {
		    		$measure = $measures[$j];

		    		$type = $measure["type"];
		    		$value = $measure["value"];
		    		if ($type == "9") {
		    			$dia = $value;
		    		} else if ($type == "10") {
		    			$sys = $value;
		    		} else if ($type == "11") {
		    			$pulse = $value;
		    		}
		    	}
		    	
		    	$sql = "INSERT IGNORE INTO M66tmine VALUES ( FROM_UNIXTIME(" . $date . "), " . $user_personal_id . ", " . $sys . ", " . $dia . ", " . $pulse . " );";
		    	if ($conn->query($sql) === TRUE) {
				    echo "Successfully added BP data entry to db...";
				} else {
					echo mysqli_error($conn) . "<br>";
				}
		    }
		}

		print_r("all ok1<br>");

		$sql = "SELECT * FROM M66tmine;";
        $result = $conn->query($sql);
        
        print_r("all ok2<br>");

        if ($result->num_rows > 0) {
        	print_r("all ok3<br>");
            while($row = $result->fetch_assoc()) {
                echo $row["Aeg"] . " " . $row["Patsiendi_isikukood"] . " " . $row["Systoolne"] . " " . $row["Diastoolne"] . " " . $row["Pulss"] . "<br>";
            }
            print_r("all ok4<br>");
        } else {
            echo "0 results<br>";
        }

		$conn->close();
		?>

	</body>
</html>