<?php
	require_once '../../site/functions.php';

	echo json_encode(repost($_POST['key'],$_POST['id'],$_POST['caption']));
?>