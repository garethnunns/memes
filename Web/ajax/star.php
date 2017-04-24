<?php
	require_once '../site/web.php';
	check();

	echo json_encode(star($_SESSION['key'],$_POST['id']));
?>