$(document).ready(function () {

    makeAJAXRequestPatient();

    $("#patsiendi-vaade").click(function () {
        makeAJAXRequestPatient();
    });

    $("#arsti-vaade").click(function () {
        makeAJAXRequestDoctor();
    });

});

function makeAJAXRequestPatient() {
    $.ajax({
        type: "POST",
        data: { "patsient": "" },
        url: "select_vw_PatsiendiVaade.php",
        success: function(res) {
            $("#view").html(res);
        }
    });
}

function makeAJAXRequestDoctor() {
    $.ajax({
        type: "POST",
        data: { "arst": "" },
        url: "select_vw_ArstiVaade.php",
        success: function(res) {
            $("#view").html(res);
        }
    });
}