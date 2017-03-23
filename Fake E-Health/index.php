<!DOCTYPE html>
<html lang="et">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Minu-tervis</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
    <link rel="stylesheet" type="text/css" href="stylesheet.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
    <script src="script.js"></script>
</head>
<body>

    <div id="wrap">

        <div class="jumbotron">
            <div class="container">
                <h1>Tere tulemast Minu-tervisesse!</h1>
                <p style="margin-left: 12px;">Minu-tervis on veebisüsteem, mis kogub sinu terviseandmeid.</p>
            </div>
        </div>

        <div class="container">

            <div class="dropdown text-right">
                <button class="btn btn-default dropdown-toggle" type="button" id="dropdownMenu1" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
                    Muuda vaade...
                    <span class="caret"></span>
                </button>
                <ul class="dropdown-menu dropdown-menu-right" aria-labelledby="dropdownMenu1">
                    <li id="patsiendi-vaade"><a href="#">Patsiendi vaade</a></li>
                    <li id="arsti-vaade"><a href="#">Arsti vaade</a></li>
                </ul>
            </div>

            <?php

            require("db.php");

            // if the mobile app sent us new data, then parse it and add to db
            if ($json = file_get_contents("php://input") !== false){

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
                            echo "Successfully added BP data entry to db...<br>";
                        } else {
                            echo "Could not add BP data to db: " . mysqli_error($conn) . "<br>";
                        }
                    }
                }

            }

            $conn->close();

            ?>

            <div id='view'></div>

        </div>

    </div>

    <div class="text-right" id="footer">
        <p>Anna Bass</p>
    </div>

</body>
</html>