<?php
	require_once '../../site/functions.php';

	echo json_encode(followers($_POST['key'],$_POST['id'],$_POST['page']));
?>