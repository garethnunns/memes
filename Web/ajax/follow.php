<?php
	require_once '../site/web.php';
	check();



	echo json_encode(array(follow($_SESSION['key'],$_POST['id'])));
?>