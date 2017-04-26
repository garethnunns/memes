<?php
	require_once '../site/web.php';
	check();

	echo json_encode(repost($_SESSION['key'],$_POST['id'],$_POST['caption']));
?>