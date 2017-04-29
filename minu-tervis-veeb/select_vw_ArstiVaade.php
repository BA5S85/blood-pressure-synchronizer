<?php

require("db.php");

// show doctor view
$sql = "SELECT * FROM vw_ArstiVaade ORDER BY Aeg DESC;";
$result = $conn->query($sql);

if ($result->num_rows > 0) {
    echo "<table class='table table-striped table-bordered table-hover'>";
    echo "<tr>";
    echo "<th>Patsiendi isikukood</th>";
    echo "<th>Patsiendi eesnimi</th>";
    echo "<th>Patsiendi perekonnanimi</th>";
    echo "<th>Mõõtmise kuupäev</th>";
    echo "<th>Süstoolne vererõhk</th>";
    echo "<th>Diastoolne vererõhk</th>";
    echo "<th>Pulss</th>";
    echo "</tr>";
    while ($row = $result->fetch_assoc()) {
        echo "<tr>";
        echo "<td>" . $row["Isikukood"] . "</td>";
        echo "<td>" . $row["Eesnimi"] . "</td>";
        echo "<td>" . $row["Perekonnanimi"] . "</td>";
        echo "<td>" . $row["Aeg"] . "</td>";
		
		$green = "#ddf4d9";
        $orange = "#ffebd1";
        $red = "#ffd5ce";

        $sys = $row["Systoolne"];
        $style = "";
        if ($sys < 120) $style = $green;
        else if ($sys < 140) $style = $orange;
        else $style = $red;
        echo "<td style='background-color: " . $style . "'>" . $row["Systoolne"] . "</td>";

        $dia = $row["Diastoolne"];
        $style = "";
        if ($dia < 80) $style = $green;
        else if ($dia < 90) $style = $orange;
        else $style = $red;
        echo "<td style='background-color: " . $style . "'>" . $row["Diastoolne"] . "</td>";
		
        echo "<td>" . $row["Pulss"] . "</td>";
        echo "</tr>";
    }
    echo "</table>";
} else {
    die("'SELECT * FROM vw_ArstiVaade ORDER BY Aeg DESC;' query returned 0 rows");
}