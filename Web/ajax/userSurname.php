<?php
	require_once '../site/web.php';
	check();

	header("refresh:5; url=/settings"); // fallback for none JS browsers
	echo json_encode(setUserSurname($_SESSION['key'],$_POST['text']));
?>