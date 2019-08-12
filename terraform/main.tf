provider "google" {
  project = "bg-search"
  region  = "us-east1"
  version = "2.2.0"
}

resource "google_pubsub_topic" "default" {
  name = "pull"
}

resource "google_cloud_scheduler_job" "default" {
  name        = "pull-job"
  description = "Emit 'pull' every 15 minutes"
  schedule    = "*/15 * * * *"

  pubsub_target {
    topic_name = "${google_pubsub_topic.default.id}"
    data       = "${base64encode("_")}"
  }
}
