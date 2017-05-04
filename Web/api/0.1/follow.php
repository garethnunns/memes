<?php
	require_once '../../site/functions.php';

	echo json_encode(follow($_POST['key'],$_POST['id']));
?>