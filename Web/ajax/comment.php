<?php
	require_once '../site/web.php';
	check();

	echo json_encode(comment($_SESSION['key'],$_POST['id'],$_POST['comment']));
?>