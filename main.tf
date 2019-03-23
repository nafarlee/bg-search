provider "google" {
  project = "bg-search"
  region  = "us-east1"
  version = "2.2.0"
}

resource "google_pubsub_topic" "default" {
  name = "pull"
}
