<?php
	require_once '../../site/functions.php';

	echo json_encode(comment($_POST['key'],$_POST['id'],$_POST['comment']));
?>