#!/usr/bin/env bash
echo -n "$1""$2""$3" | sha1sum | head -c 40

# createattendeePW=5ac6a355-a2f9-4de8-9213-052061233d6b&name=Demo%2520Kurs&tutorPW=bf7efbd3-13b8-47b8-9e3c-ef0c8ba47192&meta_bbb-origin-version=v2&meetingID=60b1a826-4348-44a2-be62-e3b85806238b&moderatorPW=091fd7d8-f539-4d9f-a53c-c69c8a648ad4&meta_bbb-origin=Greenlight&meta_bbb-origin-server-name=feedback.mni.thm.de8Dsupersecurekeydf0
# createattendeePW=5ac6a355-a2f9-4de8-9213-052061233d6b&name=Demo%20Kurs&tutorPW=bf7efbd3-13b8-47b8-9e3c-ef0c8ba47192&meta_bbb-origin-version=v2&meetingID=60b1a826-4348-44a2-be62-e3b85806238b&moderatorPW=091fd7d8-f539-4d9f-a53c-c69c8a648ad4&meta_bbb-origin=Greenlight&meta_bbb-origin-server-name=feedback.mni.thm.de8Dsupersecurekeydf0